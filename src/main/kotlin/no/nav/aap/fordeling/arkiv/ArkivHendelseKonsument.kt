package no.nav.aap.fordeling.arkiv

import kotlin.random.Random.Default.nextBoolean
import no.nav.aap.fordeling.Integrasjoner
import no.nav.aap.util.Constants.JOARK
import no.nav.aap.util.LoggerUtil.getLogger
import no.nav.boot.conditionals.ConditionalOnGCP
import no.nav.boot.conditionals.EnvUtil.isDevOrLocal
import no.nav.joarkjournalfoeringhendelser.JournalfoeringHendelseRecord
import org.springframework.core.env.Environment
import org.springframework.kafka.annotation.DltHandler
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.annotation.RetryableTopic
import org.springframework.kafka.retrytopic.FixedDelayStrategy.*
import org.springframework.kafka.support.KafkaHeaders.*
import org.springframework.messaging.handler.annotation.Header
import org.springframework.retry.annotation.Backoff

@ConditionalOnGCP
class ArkivHendelseKonsument(private val fordeler: DelegerendeFordeler, private val integrasjoner: Integrasjoner, private val env: Environment) {

    val log = getLogger(javaClass)


    @KafkaListener(topics = ["teamdokumenthandtering.aapen-dok-journalfoering"], containerFactory = JOARK)
    @RetryableTopic(attempts = "#{'\${fordeling.retries:3}'}", backoff = Backoff(delayExpression = "#{'\${fordeling.backoff}'}"),fixedDelayTopicStrategy = SINGLE_TOPIC, autoCreateTopics = "false")
    fun listen(hendelse: JournalfoeringHendelseRecord, @Header(DELIVERY_ATTEMPT) attempt: String?)  {
        runCatching {
            log.info("Behandler $hendelse" + attempt?.let { " for $it. gang" } ?: "")
            with(integrasjoner) {
                if (nextBoolean() && isDevOrLocal(env))  {
                    log.info("Tvinger fram en feil i dev for å teste retry")
                    throw IllegalStateException("Dette er en tvunget feil i dev")
                }
                arkiv.hentJournalpost(hendelse.journalpostId)?.let {
                    fordeler.fordel(it,navEnhet(it))
                }?: log.warn("Ingen journalpost kunne hentes for id ${hendelse.journalpostId}")  // TODO hva gjør vi her?
            }
        }.getOrElse {
            log.warn("Behandling av $hendelse feilet",it)
            throw it
        }
    }

    @DltHandler
    fun dlt(payload: JournalfoeringHendelseRecord,
            @Header(EXCEPTION_STACKTRACE) trace: String?)  {
        log.warn("Mottok DLT hendelse  med trace $trace for $payload")
    }
}