package no.nav.aap.fordeling.arkiv.fordeling

import java.util.concurrent.atomic.AtomicInteger
import no.nav.aap.api.felles.error.IrrecoverableIntegrationException
import no.nav.aap.fordeling.arkiv.ArkivClient
import no.nav.aap.fordeling.arkiv.fordeling.FordelingConfig.Companion.FORDELING
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.FordelingResultat.FordelingType.DIREKTE_MANUELL
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.FordelingResultat.FordelingType.INGEN_JOURNALPOST
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.FordelingResultat.FordelingType.ALLEREDE_JOURNALFØRT
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.JournalpostDTO.Kanal.UKJENT
import no.nav.aap.fordeling.navenhet.EnhetsKriteria.NavOrg.NAVEnhet.Companion.FORDELINGSENHET
import no.nav.aap.fordeling.navenhet.NavEnhetUtvelger
import no.nav.aap.fordeling.slack.Slacker
import no.nav.aap.fordeling.util.MetrikkLabels.FORDELINGSTYPE
import no.nav.aap.fordeling.util.MetrikkLabels.FORDELINGTS
import no.nav.aap.fordeling.util.MetrikkLabels.KANAL
import no.nav.aap.util.CallIdGenerator
import no.nav.aap.util.LoggerUtil.getLogger
import no.nav.aap.util.MDCUtil.NAV_CALL_ID
import no.nav.aap.util.MDCUtil.toMDC
import no.nav.aap.util.Metrikker.inc
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
                log.warn("Ingen bruker er satt på journalposten, sender direkte til manuell journalføring")
                fordeler.fordelManuelt(jp, FORDELINGSENHET)
                jp.metrikker(DIREKTE_MANUELL,topic)
                return
            }

            if (jp.kanal == UKJENT)  {
                log.warn("UKjent kanal for journalpost ${jp.journalpostId}, oppdater enum og vurder håndtering")
                slack.feil("Ukjent kanal for journalpost ${jp.journalpostId}")
            }

            if (!beslutter.skalFordele(jp)) {
                log.info("Journalpost ${jp.journalpostId} med status '${jp.status}' skal IKKE fordeles (tittel='${jp.tittel}', brevkode='${jp.hovedDokumentBrevkode}')")
                jp.metrikker(ALLEREDE_JOURNALFØRT, topic)
                return
            }

            log.info("Begynner fordeling av ${jp.journalpostId} (behandlingstema='${jp.behandlingstema}', tittel='${jp.tittel}', brevkode='${jp.hovedDokumentBrevkode}', status='${jp.status}')")
            fordel(jp).also {
                jp.metrikker(it.fordelingstype, topic)
            }

        }.onFailure {
            fordelFeilet(hendelse, antallForsøk, topic, it)
        }
    }

    @DltHandler
    fun dlt(h: JournalfoeringHendelseRecord, @Header(DLT_ORIGINAL_TIMESTAMP) timestamp: String, @Header(EXCEPTION_STACKTRACE) trace: String?) =
        with("Gir opp fordeling av journalpost ${h.journalpostId}, opprinnelig mottatt $timestamp") {
            log.error(this)
            slack.feil(this)
        }

    private fun fordelFeilet(hendelse: JournalfoeringHendelseRecord, antall: Int?, topic: String, t: Throwable) : Nothing =
        with("Fordeling av journalpost ${hendelse.journalpostId} feilet for ${antall?.let { "$it." } ?: "1."} gang på topic $topic") {
            log.warn("$this (${t.javaClass.simpleName})", t)
            slack.feilHvisDev("$this. (${t.message})")
            throw t
        }

    private fun fordel(jp: Journalpost) =
        fordeler.fordel(jp, enhet.navEnhet(jp)).also {
            slack.meldingHvisDev("${it.msg()} (${jp.fnr})")
            log.info(it.msg())
        }
    private fun JournalfoeringHendelseRecord.tema() = temaNytt.lowercase()
}