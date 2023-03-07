package no.nav.aap.fordeling.arkiv.fordeling

import no.nav.aap.fordeling.arkiv.ArkivClient
import no.nav.aap.fordeling.arkiv.fordeling.FordelingConfig.Companion.FORDELING
import no.nav.aap.fordeling.config.GlobalBeanConfig.FaultInjecter
import no.nav.aap.fordeling.navenhet.NavEnhetUtvelger
import no.nav.aap.fordeling.slack.SlackNotifier
import no.nav.aap.util.LoggerUtil.getLogger
import no.nav.boot.conditionals.Cluster.Companion.currentCluster
import no.nav.boot.conditionals.ConditionalOnGCP
import no.nav.joarkjournalfoeringhendelser.JournalfoeringHendelseRecord
import org.springframework.kafka.annotation.DltHandler
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.annotation.RetryableTopic
import org.springframework.kafka.retrytopic.RetryTopicHeaders.DEFAULT_HEADER_ATTEMPTS
import org.springframework.kafka.retrytopic.RetryTopicHeaders.DEFAULT_HEADER_ORIGINAL_TIMESTAMP
import org.springframework.kafka.retrytopic.SameIntervalTopicReuseStrategy.SINGLE_TOPIC
import org.springframework.kafka.support.KafkaHeaders.*
import org.springframework.messaging.handler.annotation.Header
import org.springframework.retry.annotation.Backoff

@ConditionalOnGCP
class FordelingHendelseKonsument(
        private val fordeler: FordelingTemaDelegator,
        private val arkiv: ArkivClient,
        private val enhet: NavEnhetUtvelger,
        private val slack: SlackNotifier,
        private val faultInjecter: FaultInjecter) {

    val log = getLogger(FordelingHendelseKonsument::class.java)

    @KafkaListener(topics = ["#{'\${fordeling.topics.main}'}"], containerFactory = FORDELING)
    @RetryableTopic(attempts = "#{'\${fordeling.topics.retries}'}",
            backoff = Backoff(delayExpression = "#{'\${fordeling.topics.backoff}'}"),
            sameIntervalTopicReuseStrategy = SINGLE_TOPIC,
            autoCreateTopics = "false")
    fun listen(
            hendelse: JournalfoeringHendelseRecord,
            @Header(DEFAULT_HEADER_ATTEMPTS, required = false) forsøk: Int?,
            @Header(DEFAULT_HEADER_ORIGINAL_TIMESTAMP) timestamp: String?,
            @Header(RECEIVED_TOPIC) topic: String) {
        runCatching {
            log.info("Original timestamp $timestamp")
            log.info("Fordeler journalpost ${hendelse.journalpostId} mottatt på $topic for ${forsøk?.let { "$it." } ?: "1."} gang.")
            faultInjecter.maybeInject(this)
            arkiv.hentJournalpost("${hendelse.journalpostId}")?.let {
                fordeler.fordel(it, enhet.navEnhet(it)).also {
                    r ->
                    log.info(r.formattertMelding())
                    slack.send(r.formattertMelding())
                }
            }
                ?: log.warn("Ingen journalpost kunne hentes for journalpost ${hendelse.journalpostId}")
        }.getOrElse {
            val jp = when(it)  {
                is JournalpostAwareException -> it.journalpost
                else -> ""
            }
            with("Fordeling av journalpost ${hendelse.journalpostId} $jp feilet for ${forsøk?.let { "$it." } ?: "1."} gang") {
                log.warn(this, it)
                slack.send("$this (cluster: ${currentCluster().name.lowercase()}). (${it.message})")
            }
            throw it
        }
    }

    @DltHandler
    fun dlt(payload: JournalfoeringHendelseRecord, @Header(EXCEPTION_STACKTRACE) trace: String?) {
        log.warn("Gir opp fordeling av journalpost ${payload.journalpostId} $trace").also {
            slack.send("Journalpost ${payload.journalpostId} kunne ikke fordeles")
        }
    }
}