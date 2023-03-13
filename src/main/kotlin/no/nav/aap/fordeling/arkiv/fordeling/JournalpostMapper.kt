package no.nav.aap.fordeling.arkiv.fordeling

import no.nav.aap.api.felles.AktørId
import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.JournalpostDTO
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.JournalpostDTO.Bruker
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.JournalpostDTO.BrukerDTO
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.JournalpostDTO.BrukerDTO.BrukerType.AKTOERID
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.JournalpostDTO.BrukerDTO.BrukerType.FNR
import no.nav.aap.fordeling.person.PDLClient
import org.springframework.stereotype.Component

@Component
class JournalpostMapper(private val pdl: PDLClient) {

    fun tilJournalpost(dto: JournalpostDTO) =
       with(dto) {
           with(tilBruker(bruker)) {
               Journalpost(tittel,
                       journalfoerendeEnhet,
                       journalpostId,
                       journalstatus,
                       tema.lowercase(),
                       behandlingstema,
                       fnr,
                       this,
                       tilBruker(avsenderMottaker),
                       kanal,
                       relevanteDatoer,
                       dokumenter)
           }
       }

    fun tilBruker(dto: BrukerDTO) = Bruker(tilFnr(dto))

    private fun tilFnr(dto: BrukerDTO) =
        with(dto) {
            when(type) {
                AKTOERID -> tilFnr(id)
                FNR -> Fødselsnummer(id)
                else -> throw IllegalStateException("IdType $type ikke støttet")
            }
        }
    private fun tilFnr(aktørId: String) =
        pdl.fnr(AktørId(aktørId)) ?: throw IllegalStateException("Kunne ikke slå opp FNR for aktørid $aktørId")
}