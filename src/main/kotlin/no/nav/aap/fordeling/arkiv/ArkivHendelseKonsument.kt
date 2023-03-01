package no.nav.aap.fordeling.arkiv

import no.nav.aap.fordeling.Integrasjoner
import no.nav.aap.util.Constants.JOARK
import no.nav.aap.util.LoggerUtil.getLogger
import no.nav.boot.conditionals.ConditionalOnGCP
import no.nav.joarkjournalfoeringhendelser.JournalfoeringHendelseRecord
import org.springframework.kafka.annotation.DltHandler
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.annotation.RetryableTopic
import org.springframework.kafka.retrytopic.FixedDelayStrategy
import org.springframework.kafka.retrytopic.FixedDelayStrategy.*
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.kafka.support.KafkaHeaders.*
import org.springframework.messaging.handler.annotation.Header
import org.springframework.retry.annotation.Backoff

@ConditionalOnGCP
class ArkivHendelseKonsument(private val fordeler: DelegerendeFordeler, val integrasjoner: Integrasjoner) {

   var counter = 0

    val log = getLogger(javaClass)


    @KafkaListener(topics = ["#{'\${joark.hendelser.topic:teamdokumenthandtering.aapen-dok-journalfoering}'}"], containerFactory = JOARK)
    @RetryableTopic(attempts = "3", backoff = Backoff(delay = 1000),fixedDelayTopicStrategy = SINGLE_TOPIC, autoCreateTopics = "false")
    fun listen(payload: JournalfoeringHendelseRecord, @Header(RECEIVED_TOPIC) topic: String)  {
        runCatching {
            log.trace("Mottok hendelse $payload på topic $topic")
            if (counter++ < 3) {
                log.info("Kaster exception")
                throw IllegalArgumentException("OOPS TEST")
            }
            log.info("Fordeler")
            with(integrasjoner) {
                arkiv.hentJournalpost(payload.journalpostId)?.let {
                    fordeler.fordel(it,navEnhet(it))
                }?: log.warn("Ingen journalpost kunne slås opp for id ${payload.journalpostId}")  // TODO hva gjør vi her?
            }
        }.getOrElse { throw FordelingException(cause =  it) } // TODO tenke gjennom denne
    }

   @DltHandler
    fun dlt(payload: JournalfoeringHendelseRecord,@Header(RECEIVED_TOPIC) topic: String)  {
            log.info("Mottok DLT hendelse på $topic  $payload")
    }
}