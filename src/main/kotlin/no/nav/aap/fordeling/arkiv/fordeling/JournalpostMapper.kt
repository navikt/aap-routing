package no.nav.aap.fordeling.arkiv.fordeling

import no.nav.aap.api.felles.AktørId
import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.felles.error.IntegrationException
import no.nav.aap.api.felles.error.IrrecoverableIntegrationException
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.JournalpostDTO
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.JournalpostDTO.Bruker
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.JournalpostDTO.BrukerDTO
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.JournalpostDTO.BrukerDTO.BrukerType.AKTOERID
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.JournalpostDTO.BrukerDTO.BrukerType.FNR
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.JournalpostDTO.DokumentInfo
import no.nav.aap.fordeling.person.PDLClient
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class JournalpostMapper(private val pdl: PDLClient) {

    private val log = LoggerFactory.getLogger(JournalpostMapper::class.java)



    fun tilJournalpost(dto: JournalpostDTO) =
        with(dto) {
            val brukerFnr = bruker?.fødselsnummer(journalpostId,"'bruker'")
            val avsenderMottakerFnr = avsenderMottaker?.fødselsnummer(journalpostId,"'avsenderMottaker'")
            Journalpost(tittel,
                    journalfoerendeEnhet,
                    journalpostId,
                    journalstatus,
                    journalpostType,
                    tema.lowercase(),
                    behandlingstema,
                    brukerFnr ?: FIKTIVTFNR,
                    brukerFnr?.let { Bruker(brukerFnr) },
                    avsenderMottakerFnr?.let { AvsenderMottaker(avsenderMottakerFnr) },
                    kanal,
                    relevanteDatoer,
                    dokumenter.toSortedSet(compareBy(DokumentInfo::dokumentInfoId)),
                    tilleggsopplysninger)
        }

    private fun BrukerDTO.fødselsnummer(journalpostId: String, kind: String) =
        with(this) {
            id?.let {
                when(type) {
                    AKTOERID -> AktørId(it).fødselsnummer(journalpostId)
                    FNR -> Fødselsnummer(it)
                    else -> null.also {
                        log.warn("IdType $type ikke støttet for $kind med id $it i journalpost $journalpostId")
                    }
                }
            }
        }
    private fun AktørId.fødselsnummer(journalpostId: String) = pdl.fnr(this)?: throw IrrecoverableIntegrationException("Kunne ikke slå opp FNR for aktørid $this i journalpost $journalpostId")

    companion object {
        val FIKTIVTFNR = Fødselsnummer("08089403198")  // Fiktivt i tilfelle du lurte
    }

}