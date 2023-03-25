package no.nav.aap.fordeling.arkiv.fordeling

import java.util.concurrent.atomic.AtomicInteger
import no.nav.aap.api.felles.error.IrrecoverableIntegrationException
import no.nav.aap.fordeling.arkiv.ArkivClient
import no.nav.aap.fordeling.arkiv.fordeling.FordelingConfig.Companion.FORDELING
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.FordelingResultat.FordelingType.DIREKTE_MANUELL
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.FordelingResultat.FordelingType.INGEN_JOURNALPOST
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.FordelingResultat.FordelingType.ALLEREDE_JOURNALFØRT
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.JournalpostDTO.JournalStatus.JOURNALFOERT
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.JournalpostDTO.JournalStatus.MOTTATT
import no.nav.aap.fordeling.navenhet.EnhetsKriteria.NavOrg.NAVEnhet.Companion.FORDELINGSENHET
import no.nav.aap.fordeling.navenhet.NavEnhetUtvelger
import no.nav.aap.fordeling.slack.Slacker
import no.nav.aap.fordeling.util.MetrikkLabels.FORDELINGSTYPE
import no.nav.aap.fordeling.util.MetrikkLabels.FORDELINGTS
import no.nav.aap.fordeling.util.MetrikkLabels.KANAL
import no.nav.aap.util.CallIdGenerator
import no.nav.aap.util.LoggerUtil.getLogger
import no.nav.aap.util.MDCUtil
import no.nav.aap.util.MDCUtil.NAV_CALL_ID
import no.nav.aap.util.MDCUtil.toMDC
import no.nav.aap.util.Metrikker.inc
import no.nav.boot.conditionals.Cluster.Companion.isProd
import no.nav.boot.conditionals.ConditionalOnGCP
import no.nav.joarkjournalfoeringhendelser.JournalfoeringHendelseRecord
import org.springframework.kafka.annotation.DltHandler
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.annotation.RetryableTopic
import org.springframework.kafka.retrytopic.RetryTopicHeaders.DEFAULT_HEADER_ATTEMPTS
import org.springframework.kafka.retrytopic.SameIntervalTopicReuseStrategy.SINGLE_TOPIC
import org.springframework.kafka.support.KafkaHeaders.*
import org.springframework.messaging.handler.annotation.Header
import org.springframework.retry.annotation.Backoff
@ConditionalOnGCP
class FordelingHendelseKonsument(
        private val fordeler: FordelingFactory,
        private val arkiv: ArkivClient,
        private val enhet: NavEnhetUtvelger,
        private val beslutter: FordelingBeslutter,
        private val slack: Slacker) {

    val log = getLogger(FordelingHendelseKonsument::class.java)
    private val count = AtomicInteger(0)

    @KafkaListener(topics = ["#{'\${fordeling.topics.main}'}"], containerFactory = FORDELING)
    @RetryableTopic(attempts = "#{'\${fordeling.topics.retries}'}", backoff = Backoff(delayExpression = "#{'\${fordeling.topics.backoff}'}"),
            sameIntervalTopicReuseStrategy = SINGLE_TOPIC,
            exclude = [IrrecoverableIntegrationException::class],
            autoStartDltHandler = "true",
            autoCreateTopics = "false")

    fun listen(hendelse: JournalfoeringHendelseRecord, @Header(DEFAULT_HEADER_ATTEMPTS, required = false) antallForsøk: Int?, @Header(RECEIVED_TOPIC) topic: String) {
        runCatching {
            toMDC(NAV_CALL_ID, CallIdGenerator.create())
            log.info("Behandler journalpost ${hendelse.journalpostId} med tema ${hendelse.tema()} og status ${hendelse.journalpostStatus} på $topic for ${antallForsøk?.let { "$it." } ?: "1."} gang.")
            val jp = arkiv.hentJournalpost("${hendelse.journalpostId}")

            if (jp == null)  {
                log.warn("Ingen journalpost kunne leses fra JOARK, lar dette fanges opp av sikkerhetsnettet")
                inc(FORDELINGTS, TOPIC,topic,KANAL,hendelse.mottaksKanal,FORDELINGSTYPE, INGEN_JOURNALPOST.name)
                return
            }

            if (jp.bruker == null) {
                log.warn("Ingen bruker er satt på journalposten, går direkte til manuell journalføring")
                fordeler.fordelManuelt(jp, FORDELINGSENHET)
                jp.metrikker(DIREKTE_MANUELL,topic)
                return
            }



            if (!beslutter.skalFordele(jp)) {
                log.info("Journalpost ${jp.journalpostId} med status ${jp.status}  fordeles IKKE")
                inc(FORDELINGTS, FORDELINGSTYPE,ALLEREDE_JOURNALFØRT.name,TOPIC, topic)
                return
            }


            log.info("Fordeler ${jp.journalpostId} med brevkode ${jp.hovedDokumentBrevkode}")
            fordel(jp).also {
                jp.metrikker(it.fordelingstype, topic)
                val nyjp = arkiv.hentJournalpost(jp.journalpostId)
                if (jp.status != nyjp?.status)  {  // test siden vi ikke oppdaterer
                    log.warn("Journalpost ${jp.journalpostId} med brevkode ${nyjp.hovedDokumentBrevkode} RACE condition")
                }
            }



        }.onFailure {
            fordelFeilet(hendelse, antallForsøk, topic, it)
        }
    }

    @DltHandler
    fun dlt(h: JournalfoeringHendelseRecord, @Header(EXCEPTION_STACKTRACE) trace: String?) =
        with("Gir opp fordeling av journalpost ${h.journalpostId}") {
            log.warn(this)
            slack.okHvisdev(this)
        }

    private fun fordelFeilet(hendelse: JournalfoeringHendelseRecord, antall: Int?, topic: String, t: Throwable) : Nothing =
        with("Fordeling av journalpost ${hendelse.journalpostId} feilet for ${antall?.let { "$it." } ?: "1."} gang på topic $topic") {
            log.warn("$this (${t.javaClass.simpleName})", t)
            slack.okHvisdev("$this. (${t.message})")
            throw t
        }

    private fun fordel(jp: Journalpost) =
        fordeler.fordel(jp, enhet.navEnhet(jp)).also {
            with("${it.msg()} (${jp.fnr})") {
                log.info(this)
                slack.jippiHvisDev(this)
            }
        }
    private fun JournalfoeringHendelseRecord.tema() = temaNytt.lowercase()

}