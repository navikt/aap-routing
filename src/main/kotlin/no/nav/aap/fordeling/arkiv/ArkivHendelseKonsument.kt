package no.nav.aap.fordeling.arkiv

import no.nav.aap.util.Constants.JOARK
import no.nav.aap.util.LoggerUtil.getLogger
import no.nav.boot.conditionals.ConditionalOnGCP
import no.nav.joarkjournalfoeringhendelser.JournalfoeringHendelseRecord
import org.springframework.kafka.annotation.DltHandler
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.annotation.RetryableTopic

@ConditionalOnGCP
class ArkivHendelseKonsument(private val fordeler: DelegerendeFordeler, val arkiv: ArkivClient) {

    val log = getLogger(javaClass)

    @KafkaListener(topics = ["#{'\${joark.hendelser.topic:teamdokumenthandtering.aapen-dok-journalfoering}'}"], containerFactory = JOARK)
    fun listen(payload: JournalfoeringHendelseRecord)  {
        kotlin.runCatching {
            arkiv.journalpost(payload.journalpostId)?.let {
                log.info("Fordeler $it")
                fordeler.fordel(it).also { log.info("Fordelt $it") }
            }?: log.warn("Ingen journalpost kunne slås opp for id ${payload.journalpostId}")  // TODO hva gjør vi her?
        }.getOrElse { throw FordelingException(cause =  it) }
    }
}