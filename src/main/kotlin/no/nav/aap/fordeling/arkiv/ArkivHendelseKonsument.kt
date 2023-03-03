package no.nav.aap.fordeling.arkiv

import no.nav.aap.fordeling.Integrasjoner
import no.nav.aap.fordeling.config.GlobalBeanConfig.FaultInjecter
import no.nav.aap.fordeling.config.SlackNotifier
import no.nav.aap.util.Constants.JOARK
import no.nav.aap.util.LoggerUtil.getLogger
import no.nav.boot.conditionals.ConditionalOnGCP
import no.nav.joarkjournalfoeringhendelser.JournalfoeringHendelseRecord
import org.springframework.kafka.annotation.DltHandler
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.annotation.RetryableTopic
import org.springframework.kafka.retrytopic.FixedDelayStrategy.*
import org.springframework.kafka.retrytopic.RetryTopicHeaders.*
import org.springframework.kafka.support.KafkaHeaders.*
import org.springframework.messaging.handler.annotation.Header
import org.springframework.retry.annotation.Backoff

@ConditionalOnGCP
class ArkivHendelseKonsument(private val fordeler: DelegerendeFordeler, private val integrasjoner: Integrasjoner, private val slack: SlackNotifier,private val faultInjecter: FaultInjecter) {

    val log = getLogger(ArkivHendelseKonsument::class.java)

    @KafkaListener(topics = ["#{'\${joark.hendelser.topic:teamdokumenthandtering.aapen-dok-journalfoering}'}"], containerFactory = JOARK)
    @RetryableTopic(attempts = "#{'\${fordeling.retries}'}", backoff = Backoff(delay = 10000),fixedDelayTopicStrategy = SINGLE_TOPIC, autoCreateTopics = "false")
    fun listen(hendelse: JournalfoeringHendelseRecord,
               @Header(DEFAULT_HEADER_ATTEMPTS, required = false) forsøk: Int?,
               @Header(RECEIVED_TOPIC) topic: String)  {
        runCatching {
            log.info("Behandler $hendelse på $topic for ${forsøk?.let { "$it." } ?: "1."} gang")
            with(integrasjoner) {
                faultInjecter.inject(this@ArkivHendelseKonsument)
                arkiv.hentJournalpost(hendelse.journalpostId)?.let {
                    fordeler.fordel(it,navEnhet(it)).also { r -> log.info(r.formattertMelding()) }
                }?: log.warn("Ingen journalpost kunne hentes for id ${hendelse.journalpostId}")  // TODO hva gjør vi her?
            }
        }.getOrElse { e ->
            with("Behandling av $hendelse på $topic feilet for ${forsøk?.let { "$it." } ?: "1."} gang") {
                log.warn(this,e)
                slack.sendMessage(this)
            }
            throw e
        }
    }


    @DltHandler
    fun dlt(payload: JournalfoeringHendelseRecord,
            @Header(DELIVERY_ATTEMPT) forsøk: String?,
            @Header(EXCEPTION_STACKTRACE) trace: String?)  {
        log.warn("Gir opp behandling av $payload $trace etter $forsøk forsøk")
    }
}