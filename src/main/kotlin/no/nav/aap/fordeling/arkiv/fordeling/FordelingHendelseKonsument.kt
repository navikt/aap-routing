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
import org.springframework.kafka.support.KafkaHeaders.TOPIC
import org.springframework.messaging.handler.annotation.Header
import org.springframework.retry.annotation.Backoff
import no.nav.aap.api.felles.error.IrrecoverableIntegrationException
import no.nav.aap.fordeling.arkiv.ArkivClient
import no.nav.aap.fordeling.arkiv.fordeling.Fordeler.FordelingResultat.FordelingType.DIREKTE_MANUELL
import no.nav.aap.fordeling.arkiv.fordeling.Fordeler.FordelingResultat.FordelingType.INGEN_JOURNALPOST
import no.nav.aap.fordeling.arkiv.fordeling.FordelingConfig.Companion.FORDELING
import no.nav.aap.fordeling.arkiv.fordeling.FordelingFilter.Companion.status
import no.nav.aap.fordeling.arkiv.fordeling.JournalpostDestinasjonUtvelger.FordelingsBeslutning.ARENA
import no.nav.aap.fordeling.arkiv.fordeling.JournalpostDestinasjonUtvelger.FordelingsBeslutning.GOSYS
import no.nav.aap.fordeling.arkiv.fordeling.JournalpostDestinasjonUtvelger.FordelingsBeslutning.INGEN_DESTINASJON
import no.nav.aap.fordeling.arkiv.fordeling.JournalpostDestinasjonUtvelger.FordelingsBeslutning.KELVIN
import no.nav.aap.fordeling.navenhet.NAVEnhet.Companion.FORDELINGSENHET
import no.nav.aap.fordeling.navenhet.NavEnhetUtvelger
import no.nav.aap.fordeling.slack.SlackOperations
import no.nav.aap.fordeling.util.MetrikkKonstanter.FORDELINGSTYPE
import no.nav.aap.fordeling.util.MetrikkKonstanter.FORDELINGTS
import no.nav.aap.fordeling.util.MetrikkKonstanter.KANAL
import no.nav.aap.util.CallIdGenerator
import no.nav.aap.util.LoggerUtil.getLogger
import no.nav.aap.util.MDCUtil
import no.nav.aap.util.MDCUtil.NAV_CALL_ID
import no.nav.aap.util.MDCUtil.toMDC
import no.nav.aap.util.Metrikker.inc
import no.nav.boot.conditionals.Cluster.*
import no.nav.boot.conditionals.ConditionalOnGCP
import no.nav.joarkjournalfoeringhendelser.JournalfoeringHendelseRecord

typealias Epoch = Long

@ConditionalOnGCP
class FordelingHendelseKonsument(private val fordeler : AAPFordeler, private val arkiv : ArkivClient, private val enhet : NavEnhetUtvelger,
                                 private val utvelger : JournalpostDestinasjonUtvelger, private val slack : SlackOperations) {

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
        runCatching {
            toMDC(NAV_CALL_ID, CallIdGenerator.create())
            log.info("Mottatt hendelse for journalpost ${hendelse.journalpostId}, tema ${hendelse.temaNytt} og status ${hendelse.journalpostStatus} på $topic for ${antallForsøk?.let { "$it." } ?: "1."} gang.")

            val jp = arkiv.hentJournalpost("${hendelse.journalpostId}")

            if (jp == null) {
                log.warn("Ingen journalpost kunne leses fra JOARK, lar dette fanges opp av sikkerhetsnettet")
                inc(FORDELINGTS, TOPIC, topic, KANAL, hendelse.mottaksKanal, FORDELINGSTYPE, INGEN_JOURNALPOST.name)
                return
            }

            when (utvelger.destinasjon(jp, hendelse.status(), topic)) {

                KELVIN -> log.warn("Fordeling til Kelvin ikke implementert")

                INGEN_DESTINASJON -> {
                    log.info("Ingen fordeling av journalpost ${jp.id}, forutsetninger for fordeling ikke oppfylt")
                    return
                }

                GOSYS -> {
                    fordeler.fordelManuelt(jp, FORDELINGSENHET)
                    jp.metrikker(DIREKTE_MANUELL, topic)
                    return
                }

                ARENA -> {
                    log.info("Begynner fordeling av ${jp.id} (behandlingstema='${jp.behandlingstema}', tittel='${jp.tittel}', brevkode='${jp.hovedDokumentBrevkode}', status='${jp.status}')")
                    fordel(jp).also {
                        jp.metrikker(it.fordelingstype, topic)
                    }
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
            slack.feil(this, DEV_GCP, PROD_GCP)
        }

    private fun fordel(jp : Journalpost) =
        fordeler.fordel(jp, enhet.navEnhet(jp)).also {
            slack.rocket("$it (${jp.fnr}, ${kibanaURL()}", DEV_GCP)
            log.info("$it")
        }

    private fun fordelFeilet(hendelse : JournalfoeringHendelseRecord, antall : Int?, topic : String, t : Throwable) : Nothing =
        with("Fordeling av journalpost ${hendelse.journalpostId} feilet for ${antall?.let { "$it." } ?: "1."} gang på topic $topic") {
            log.warn("$this (${t.javaClass.simpleName})", t)
            slack.feil("$this. (${t.message})", DEV_GCP)
            throw t
        }

    private fun kibanaURL() =
        "https://logs.adeo.no/s/nav-logs-legacy/app/discover#/?_g=(filters:!(),refreshInterval:(pause:!t,value:60000),time:(from:now-15m,to:now))&_a=(columns:!(level,message,envclass,application,pod),filters:!(),interval:auto,query:(language:kuery,query:%22${MDCUtil.callId()}%22),sort:!(!('@timestamp',desc)))"

    private fun Epoch?.asDate() = this?.let { ofEpochMilli(it).atZone(systemDefault()).toLocalDateTime() }

    override fun toString() = "FordelingHendelseKonsument(fordeler=$fordeler, arkiv=$arkiv, enhet=$enhet, beslutter=$utvelger, slack=$slack)"
}