package no.nav.aap.fordeling.arkiv

import kotlin.random.Random
import kotlin.random.Random.Default.nextBoolean
import no.nav.aap.fordeling.Integrasjoner
import no.nav.aap.util.Constants.JOARK
import no.nav.aap.util.LoggerUtil.getLogger
import no.nav.boot.conditionals.ConditionalOnGCP
import no.nav.joarkjournalfoeringhendelser.JournalfoeringHendelseRecord
import org.springframework.kafka.annotation.DltHandler
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.annotation.RetryableTopic
import org.springframework.kafka.retrytopic.FixedDelayStrategy.*
import org.springframework.kafka.support.KafkaHeaders.*
import org.springframework.messaging.handler.annotation.Header
import org.springframework.retry.annotation.Backoff

@ConditionalOnGCP
class ArkivHendelseKonsument(private val fordeler: DelegerendeFordeler, val integrasjoner: Integrasjoner) {

    val log = getLogger(javaClass)


    @KafkaListener(topics = ["#{'\${joark.hendelser.topic:teamdokumenthandtering.aapen-dok-journalfoering}'}"], containerFactory = JOARK)
    @RetryableTopic(attempts = "3", backoff = Backoff(delay = 1000),fixedDelayTopicStrategy = SINGLE_TOPIC, autoCreateTopics = "false")
    fun listen(payload: JournalfoeringHendelseRecord, @Header(RECEIVED_TOPIC) topic: String)  {
        runCatching {
            log.trace("Mottok hendelse $payload på topic $topic")
            with(integrasjoner) {
                if (nextBoolean())  {
                    log.info("Tvinger fram en feil")
                    throw IllegalStateException("Dette er en tvunget feil")
                }
                arkiv.hentJournalpost(payload.journalpostId)?.let {
                    fordeler.fordel(it,navEnhet(it))
                }?: log.warn("Ingen journalpost kunne hentes for id ${payload.journalpostId}")  // TODO hva gjør vi her?
            }
        }.getOrElse {
            log.warn("Behandling av $payload feilet",it)
            throw it
        }
    }

   @DltHandler
    fun dlt(payload: JournalfoeringHendelseRecord,@Header(RECEIVED_TOPIC) topic: String,@Header(DLT_EXCEPTION_MESSAGE) msg: String,@Header(
           DLT_EXCEPTION_STACKTRACE) trace: String)  {
            log.info("Mottok DLT hendelse $msg på $topic med trace $trace  for $payload")
    }
}