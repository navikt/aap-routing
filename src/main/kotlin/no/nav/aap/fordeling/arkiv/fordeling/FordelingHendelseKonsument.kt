package no.nav.aap.fordeling.arkiv.fordeling

import org.springframework.kafka.annotation.DltHandler
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.annotation.RetryableTopic
import org.springframework.kafka.retrytopic.DltStrategy.FAIL_ON_ERROR
import org.springframework.kafka.retrytopic.RetryTopicHeaders.DEFAULT_HEADER_ATTEMPTS
import org.springframework.kafka.retrytopic.SameIntervalTopicReuseStrategy.SINGLE_TOPIC
import org.springframework.kafka.support.KafkaHeaders.EXCEPTION_STACKTRACE
import org.springframework.kafka.support.KafkaHeaders.ORIGINAL_TIMESTAMP
import org.springframework.kafka.support.KafkaHeaders.RECEIVED_TOPIC
import org.springframework.kafka.support.KafkaHeaders.TOPIC
import org.springframework.messaging.handler.annotation.Header
import org.springframework.retry.annotation.Backoff
import no.nav.aap.api.felles.error.IrrecoverableIntegrationException
import no.nav.aap.fordeling.arkiv.ArkivClient
import no.nav.aap.fordeling.arkiv.fordeling.Fordeler.FordelingResultat.FordelingType.ALLEREDE_JOURNALFØRT
import no.nav.aap.fordeling.arkiv.fordeling.Fordeler.FordelingResultat.FordelingType.DIREKTE_MANUELL
import no.nav.aap.fordeling.arkiv.fordeling.Fordeler.FordelingResultat.FordelingType.INGEN
import no.nav.aap.fordeling.arkiv.fordeling.Fordeler.FordelingResultat.FordelingType.INGEN_JOURNALPOST
import no.nav.aap.fordeling.arkiv.fordeling.FordelingConfig.Companion.FORDELING
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.JournalpostDTO.Kanal.UKJENT
import no.nav.aap.fordeling.arkiv.fordeling.Journalpost.JournalpostStatus.JOURNALFØRT
import no.nav.aap.fordeling.navenhet.NAVEnhet.Companion.FORDELINGSENHET
import no.nav.aap.fordeling.navenhet.NavEnhetUtvelger
import no.nav.aap.fordeling.slack.Slacker
import no.nav.aap.fordeling.util.MetrikkKonstanter.FORDELINGSTYPE
import no.nav.aap.fordeling.util.MetrikkKonstanter.FORDELINGTS
import no.nav.aap.fordeling.util.MetrikkKonstanter.KANAL
import no.nav.aap.util.CallIdGenerator
import no.nav.aap.util.ChaosMonkey
import no.nav.aap.util.ChaosMonkey.MonkeyExceptionType.RECOVERABLE
import no.nav.aap.util.LoggerUtil.getLogger
import no.nav.aap.util.MDCUtil.NAV_CALL_ID
import no.nav.aap.util.MDCUtil.toMDC
import no.nav.aap.util.Metrikker.inc
import no.nav.boot.conditionals.Cluster.Companion.devClusters
import no.nav.boot.conditionals.ConditionalOnGCP
import no.nav.joarkjournalfoeringhendelser.JournalfoeringHendelseRecord

@ConditionalOnGCP
class FordelingHendelseKonsument(private val fordeler : FordelingFactory, private val arkiv : ArkivClient, private val enhet : NavEnhetUtvelger,
                                 private val beslutter : FordelingBeslutter, private val monkey : ChaosMonkey, private val slack : Slacker) {

    val log = getLogger(FordelingHendelseKonsument::class.java)

    @KafkaListener(topics = ["#{'\${fordeling.topics.main}'}"], containerFactory = FORDELING)
    @RetryableTopic(attempts = "#{'\${fordeling.topics.retries}'}", backoff = Backoff(delayExpression = "#{'\${fordeling.topics.backoff}'}"),
        sameIntervalTopicReuseStrategy = SINGLE_TOPIC,
        exclude = [IrrecoverableIntegrationException::class],
        dltStrategy = FAIL_ON_ERROR,
        autoStartDltHandler = "true",
        autoCreateTopics = "false")

    fun listen(hendelse : JournalfoeringHendelseRecord, @Header(DEFAULT_HEADER_ATTEMPTS, required = false) antallForsøk : Int?,
               @Header(RECEIVED_TOPIC) topic : String) {
        runCatching {
            monkey.injectFault(FordelingHendelseKonsument::class.java.simpleName, RECOVERABLE, monkey.criteria(devClusters(), 10))
            toMDC(NAV_CALL_ID, CallIdGenerator.create())
            log.info("Behandler hendelse for journalpost ${hendelse.journalpostId}, tema ${hendelse.tema()} og status ${hendelse.journalpostStatus} på $topic for ${antallForsøk?.let { "$it." } ?: "1."} gang.")
            val jp = arkiv.hentJournalpost("${hendelse.journalpostId}")

            if (jp == null) {
                log.warn("Ingen journalpost kunne leses fra JOARK, lar dette fanges opp av sikkerhetsnettet")
                inc(FORDELINGTS, TOPIC, topic, KANAL, hendelse.mottaksKanal, FORDELINGSTYPE, INGEN_JOURNALPOST.name)
                return
            }

            if (jp.status == JOURNALFØRT) {
                log.info("Journalpost ${jp.id}  er allerde journalført  (tittel='${jp.tittel}', brevkode='${jp.hovedDokumentBrevkode}')")
                jp.metrikker(ALLEREDE_JOURNALFØRT, topic)
                return
            }

            if (jp.bruker == null) {
                log.warn("Ingen bruker er satt på journalposten, sender direkte til manuell journalføring")
                fordeler.fordelManuelt(jp, FORDELINGSENHET)
                jp.metrikker(DIREKTE_MANUELL, topic)
                return
            }

            if (!beslutter.skalFordele(jp)) {
                log.info("Journalpost ${jp.id} med status '${jp.status}' skal IKKE fordeles (tittel='${jp.tittel}', brevkode='${jp.hovedDokumentBrevkode}')")
                jp.metrikker(INGEN, topic)
                return
            }

            if (jp.kanal == UKJENT) {
                log.warn("UKjent kanal for journalpost ${jp.id}, oppdater enum og vurder håndtering")
                slack.feil("Ukjent kanal for journalpost ${jp.id}")
            }

            log.info("Begynner fordeling av ${jp.id} (behandlingstema='${jp.behandlingstema}', tittel='${jp.tittel}', brevkode='${jp.hovedDokumentBrevkode}', status='${jp.status}')")
            fordel(jp).also {
                jp.metrikker(it.fordelingstype, topic)
            }

        }.onFailure {
            fordelFeilet(hendelse, antallForsøk, topic, it)
        }
    }

    @DltHandler
    fun dlt(h : JournalfoeringHendelseRecord, @Header(ORIGINAL_TIMESTAMP) timestamp : String?, @Header(EXCEPTION_STACKTRACE) trace : String?) =
        with("Gir opp fordeling av journalpost ${h.journalpostId}, opprinnelig hendelse ble mottatt $timestamp ") {
            log.error(this)
            slack.feil(this)
        }

    private fun fordelFeilet(hendelse : JournalfoeringHendelseRecord, antall : Int?, topic : String, t : Throwable) : Nothing =
        with("Fordeling av journalpost ${hendelse.journalpostId} feilet for ${antall?.let { "$it." } ?: "1."} gang på topic $topic") {
            log.warn("$this (${t.javaClass.simpleName})", t)
            slack.feilHvisDev("$this. (${t.message})")
            throw t
        }

    private fun fordel(jp : Journalpost) =
        fordeler.fordel(jp, enhet.navEnhet(jp)).also {
            slack.meldingHvisDev("${it.msg()} (${jp.fnr})")
            log.info(it.msg())
        }

    private fun JournalfoeringHendelseRecord.tema() = temaNytt.lowercase()

    override fun toString() = "FordelingHendelseKonsument(fordeler=$fordeler, arkiv=$arkiv, enhet=$enhet, beslutter=$beslutter, monkey=$monkey, slack=$slack)"
}