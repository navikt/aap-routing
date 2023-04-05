package no.nav.aap.fordeling.arkiv.fordeling

import java.time.Instant.*
import java.time.ZoneId.*
import org.springframework.kafka.annotation.DltHandler
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.annotation.RetryableTopic
import org.springframework.kafka.retrytopic.DltStrategy.FAIL_ON_ERROR
import org.springframework.kafka.retrytopic.RetryTopicHeaders.DEFAULT_HEADER_ATTEMPTS
import org.springframework.kafka.retrytopic.SameIntervalTopicReuseStrategy.SINGLE_TOPIC
import org.springframework.kafka.support.KafkaHeaders.EXCEPTION_STACKTRACE
import org.springframework.kafka.support.KafkaHeaders.ORIGINAL_TIMESTAMP
import org.springframework.kafka.support.KafkaHeaders.RECEIVED_TOPIC
import org.springframework.messaging.handler.annotation.Header
import org.springframework.retry.annotation.Backoff
import org.springframework.stereotype.Component
import no.nav.aap.api.felles.error.IrrecoverableIntegrationException
import no.nav.aap.fordeling.arkiv.ArkivClient
import no.nav.aap.fordeling.arkiv.fordeling.DestinasjonUtvelger.Destinasjon
import no.nav.aap.fordeling.arkiv.fordeling.DestinasjonUtvelger.Destinasjon.ARENA
import no.nav.aap.fordeling.arkiv.fordeling.DestinasjonUtvelger.Destinasjon.GOSYS
import no.nav.aap.fordeling.arkiv.fordeling.DestinasjonUtvelger.Destinasjon.INGEN_DESTINASJON
import no.nav.aap.fordeling.arkiv.fordeling.DestinasjonUtvelger.Destinasjon.KELVIN
import no.nav.aap.fordeling.arkiv.fordeling.Fordeler.FordelingResultat.FordelingType.DIREKTE_MANUELL
import no.nav.aap.fordeling.arkiv.fordeling.FordelingConfig.Companion.FORDELING
import no.nav.aap.fordeling.arkiv.fordeling.FordelingFilter.Companion.status
import no.nav.aap.fordeling.navenhet.NAVEnhet.Companion.FORDELINGSENHET
import no.nav.aap.fordeling.navenhet.NavEnhetUtvelger
import no.nav.aap.fordeling.slack.Slacker
import no.nav.aap.util.CallIdGenerator
import no.nav.aap.util.LoggerUtil.getLogger
import no.nav.aap.util.MDCUtil.NAV_CALL_ID
import no.nav.aap.util.MDCUtil.toMDC
import no.nav.boot.conditionals.ConditionalOnGCP
import no.nav.joarkjournalfoeringhendelser.JournalfoeringHendelseRecord

typealias Epoch = Long

@ConditionalOnGCP
class FordelingHendelseKonsument(private val fordeler : DestinasjonFordeler, private val arkiv : ArkivClient,
                                 private val utvelger : DestinasjonUtvelger, private val slack : Slacker) {

    private val log = getLogger(FordelingHendelseKonsument::class.java)

    @KafkaListener(topics = ["#{'\${fordeling.topics.main}'}"], containerFactory = FORDELING)
    @RetryableTopic(attempts = "#{'\${fordeling.topics.retries}'}", backoff = Backoff(delayExpression = "#{'\${fordeling.topics.backoff}'}"),
        sameIntervalTopicReuseStrategy = SINGLE_TOPIC,
        exclude = [IrrecoverableIntegrationException::class],
        dltStrategy = FAIL_ON_ERROR,
        autoStartDltHandler = "true",
        autoCreateTopics = "false")

    fun listen(hendelse : JournalfoeringHendelseRecord, @Header(DEFAULT_HEADER_ATTEMPTS, required = false) antallForsøk : Int?,
               @Header(RECEIVED_TOPIC) topic : String) {
        toMDC(NAV_CALL_ID, CallIdGenerator.create())
        runCatching {
            with(hendelse) {
                log.info("Mottatt hendelse for journalpost $journalpostId, tema $temaNytt og status $journalpostStatus på $topic for ${antallForsøk?.let { "$it." } ?: "1."} gang.")
                arkiv.hentJournalpost("$journalpostId").also {
                    fordeler.fordel(it, utvelger.destinasjon(it, status()))
                }
            }
        }.onFailure {
            fordelFeilet(hendelse, antallForsøk, topic, it)
        }
    }

    @DltHandler
    fun dlt(h : JournalfoeringHendelseRecord, @Header(ORIGINAL_TIMESTAMP) timestamp : Epoch?, @Header(EXCEPTION_STACKTRACE) trace : String?) =
        with("Gir opp fordeling av journalpost ${h.journalpostId}, opprinnelig hendelse ble mottatt ${timestamp.asDate()}") {
            log.error(this)
            slack.feil(this)
        }

    private fun fordelFeilet(hendelse : JournalfoeringHendelseRecord, antall : Int?, topic : String, t : Throwable) : Nothing =
        with("Fordeling av journalpost ${hendelse.journalpostId} feilet for ${antall?.let { "$it." } ?: "1."} gang på topic $topic") {
            log.warn("$this (${t.javaClass.simpleName})", t)
            slack.feilHvisDev("$this. (${t.message})")
            throw t
        }

    private fun Epoch?.asDate() = this?.let { ofEpochMilli(it).atZone(systemDefault()).toLocalDateTime() }

    override fun toString() = "FordelingHendelseKonsument( arkiv=$arkiv, beslutter=$utvelger, slack=$slack)"
}

@Component
class DestinasjonFordeler(private val fordeler : FordelingFactory, private val enhet : NavEnhetUtvelger, private val slack : Slacker) {

    private val log = getLogger(DestinasjonFordeler::class.java)

    fun fordel(jp : Journalpost, destinasjon : Destinasjon) {

        when (destinasjon) {

            KELVIN -> log.warn("Fordeling til Kelvin ikke implementert")

            INGEN_DESTINASJON -> {
                log.info("Ingen fordeling av journalpost ${jp.id}, forutsetninger for fordeling ikke oppfylt")
            }

            GOSYS -> {
                fordeler.fordelManuelt(jp, FORDELINGSENHET)
                jp.metrikker(DIREKTE_MANUELL)
            }

            ARENA -> {
                log.info("Begynner fordeling av ${jp.id} (behandlingstema='${jp.behandlingstema}', tittel='${jp.tittel}', brevkode='${jp.hovedDokumentBrevkode}', status='${jp.status}')")
                fordel(jp)
            }
        }
    }

    private fun fordel(jp : Journalpost) =
        fordeler.fordel(jp, enhet.navEnhet(jp)).also {
            slack.meldingHvisDev("$it (${jp.fnr})")
            log.info("$it")
            jp.metrikker(it.fordelingstype)
        }
}