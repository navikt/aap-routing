package no.nav.aap.fordeling.fordeling

import java.time.Instant.*
import java.time.ZoneId.*
import org.slf4j.MDC
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
import no.nav.aap.fordeling.arkiv.journalpost.Journalpost
import no.nav.aap.fordeling.fordeling.Fordeler.FordelingResultat.FordelingType.DIREKTE_MANUELL
import no.nav.aap.fordeling.fordeling.Fordeler.FordelingResultat.FordelingType.INGEN_JOURNALPOST
import no.nav.aap.fordeling.fordeling.FordelingAvOppgaveUtvelger.FordelingsBeslutning.ARENA
import no.nav.aap.fordeling.fordeling.FordelingAvOppgaveUtvelger.FordelingsBeslutning.GOSYS
import no.nav.aap.fordeling.fordeling.FordelingAvOppgaveUtvelger.FordelingsBeslutning.INGEN_DESTINASJON
import no.nav.aap.fordeling.fordeling.FordelingAvOppgaveUtvelger.FordelingsBeslutning.KELVIN
import no.nav.aap.fordeling.fordeling.FordelingConfig.Companion.FORDELING
import no.nav.aap.fordeling.fordeling.FordelingFilter.Companion.status
import no.nav.aap.fordeling.navenhet.NAVEnhet.Companion.FORDELINGSENHET
import no.nav.aap.fordeling.navenhet.NavEnhetUtvelger
import no.nav.aap.fordeling.slack.SlackOperations
import no.nav.aap.fordeling.util.MetrikkKonstanter.FORDELINGSTYPE
import no.nav.aap.fordeling.util.MetrikkKonstanter.FORDELINGTS
import no.nav.aap.fordeling.util.MetrikkKonstanter.KANAL
import no.nav.aap.util.CallIdGenerator
import no.nav.aap.util.LoggerUtil.getLogger
import no.nav.aap.util.LoggerUtil.kibanaURL
import no.nav.aap.util.MDCUtil.NAV_CALL_ID
import no.nav.aap.util.MDCUtil.toMDC
import no.nav.aap.util.Metrikker.inc
import no.nav.boot.conditionals.Cluster.*
import no.nav.boot.conditionals.ConditionalOnGCP
import no.nav.joarkjournalfoeringhendelser.JournalfoeringHendelseRecord

typealias Epoch = Long

@ConditionalOnGCP
class FordelingHendelseKonsument(private val fordeler : AAPFordeler, private val arkiv : ArkivClient, private val enhet : NavEnhetUtvelger,
                                 private val utvelger : FordelingAvOppgaveUtvelger, private val slack : SlackOperations,
                                 private val cfg : FordelingConfig
                                ) {

    private val log = getLogger(FordelingHendelseKonsument::class.java)

    @KafkaListener(topics = ["#{'\${fordeling.topics.main}'}"], containerFactory = FORDELING)
    @RetryableTopic(attempts = "#{'\${fordeling.topics.retries}'}", backoff = Backoff(delayExpression = "#{'\${fordeling.topics.backoff}'}"),
        sameIntervalTopicReuseStrategy = SINGLE_TOPIC, exclude = [IrrecoverableIntegrationException::class],
        dltStrategy = FAIL_ON_ERROR, autoStartDltHandler = "true", autoCreateTopics = "false")

    fun listen(hendelse : JournalfoeringHendelseRecord, @Header(DEFAULT_HEADER_ATTEMPTS, required = false) antallForsøk : Int?,
               @Header(RECEIVED_TOPIC) topic : String) {
        runCatching {
            log.info("${MDC.getCopyOfContextMap()} Mottatt hendelse for journalpost ${hendelse.journalpostId}, tema ${hendelse.temaNytt} og status ${hendelse.journalpostStatus} på $topic for ${antallForsøk?.let { "$it." } ?: "1."} gang.")
            val jp = arkiv.hentJournalpost("${hendelse.journalpostId}").also {
                toMDC(NAV_CALL_ID, "${it?.eksternReferanseId}", CallIdGenerator.create())
            }

            if (jp == null) {
                log.warn("Ingen journalpost kunne leses fra JOARK, lar dette fanges opp av sikkerhetsnettet")
                inc(FORDELINGTS, TOPIC, topic, KANAL, hendelse.mottaksKanal, FORDELINGSTYPE, INGEN_JOURNALPOST.name)
                return
            }

            log.trace("Fordeler journalpost {}", jp)
            when (utvelger.destinasjon(jp, hendelse.status(), topic)) {

                KELVIN -> throw NotImplementedError("Fordeling til Kelvin ikke implementert")

                INGEN_DESTINASJON -> log.info("Ingen fordeling av journalpost ${jp.id}, forutsetninger for fordeling ikke oppfylt")

                GOSYS -> fordeler.fordelManuelt(jp, FORDELINGSENHET).also {
                    jp.metrikker(DIREKTE_MANUELL, topic)

                }

                ARENA -> fordel(jp).also {
                    jp.metrikker(it.fordelingstype, topic)
                }
            }
        }.onFailure {
            fordelFeilet(hendelse, antallForsøk, topic, it)
        }
    }

    @DltHandler
    fun dlt(h : JournalfoeringHendelseRecord, @Header(ORIGINAL_TIMESTAMP) timestamp : Epoch?, @Header(EXCEPTION_STACKTRACE) trace : String?) =
        with("Gir opp fordeling av journalpost ${h.journalpostId} etter ${cfg.topics.retries} forsøk, opprinnelig hendelse ble mottatt ${timestamp.asDate()}") {
            log.error(this)
            slack.feil(this, DEV_GCP, PROD_GCP)
        }

    private fun fordel(jp : Journalpost) =
        fordeler.fordel(jp, enhet.navEnhet(jp)).also {
            slack.rocket("$it (${jp.fnr}, ${kibanaURL("asc")}", DEV_GCP)
            log.info("$it")
        }

    private fun fordelFeilet(hendelse : JournalfoeringHendelseRecord, antall : Int?, topic : String, t : Throwable) : Nothing =
        with("Fordeling av journalpost ${hendelse.journalpostId} feilet for ${antall?.let { "$it." } ?: "1."} gang på topic $topic") {
            log.warn("$this (${t.javaClass.simpleName})", t)
            slack.feil("$this. (${t.message})", DEV_GCP)
            throw t
        }

    private fun Epoch?.asDate() = this?.let { ofEpochMilli(it).atZone(of("Europe/Oslo")).toLocalDateTime() }

    override fun toString() = "FordelingHendelseKonsument(fordeler=$fordeler, arkiv=$arkiv, enhet=$enhet, beslutter=$utvelger, slack=$slack)"
}