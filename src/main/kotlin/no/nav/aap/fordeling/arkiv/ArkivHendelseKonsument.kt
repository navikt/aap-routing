package no.nav.aap.fordeling.arkiv

import no.nav.aap.fordeling.Integrasjoner
import no.nav.aap.util.Constants.JOARK
import no.nav.aap.util.LoggerUtil.getLogger
import no.nav.boot.conditionals.ConditionalOnGCP
import no.nav.joarkjournalfoeringhendelser.JournalfoeringHendelseRecord
import org.springframework.kafka.annotation.KafkaListener

@ConditionalOnGCP
class ArkivHendelseKonsument(private val fordeler: DelegerendeFordeler, val integrasjoner: Integrasjoner) {

    val log = getLogger(javaClass)

    @KafkaListener(topics = ["#{'\${joark.hendelser.topic:teamdokumenthandtering.aapen-dok-journalfoering}'}"], containerFactory = JOARK)
    fun listen(payload: JournalfoeringHendelseRecord)  {
        runCatching {
            with(integrasjoner) {
                arkiv.hentJournalpost(payload.journalpostId)?.let {
                    fordeler.fordel(it,navEnhet(it))
                }?: log.warn("Ingen journalpost kunne slås opp for id ${payload.journalpostId}")  // TODO hva gjør vi her?
            }
        }.getOrElse { throw FordelingException(cause =  it) } // TODO tenke gjennom denne
    }
}