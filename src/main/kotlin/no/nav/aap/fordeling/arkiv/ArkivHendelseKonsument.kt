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
import org.springframework.retry.annotation.Backoff

@ConditionalOnGCP
class ArkivHendelseKonsument(private val fordeler: DelegerendeFordeler, val integrasjoner: Integrasjoner) {

    val log = getLogger(javaClass)

    @KafkaListener(topics = ["teamdokumenthandtering.aapen-dok-journalfoering","aap.main"], containerFactory = JOARK)
    @RetryableTopic(attempts = "1", backoff = Backoff(delay = 1000),fixedDelayTopicStrategy = SINGLE_TOPIC)
    fun listen(payload: JournalfoeringHendelseRecord)  {
        runCatching {
            log.trace("Mottok hendelse $payload")
            with(integrasjoner) {
                arkiv.hentJournalpost(payload.journalpostId)?.let {
                    fordeler.fordel(it,navEnhet(it))
                }?: log.warn("Ingen journalpost kunne slås opp for id ${payload.journalpostId}")  // TODO hva gjør vi her?
            }
        }.getOrElse { throw FordelingException(cause =  it) } // TODO tenke gjennom denne
    }

   @DltHandler
    fun dlt(payload: JournalfoeringHendelseRecord)  {
            log.info("Mottok hendelse retry $payload")
    }
}