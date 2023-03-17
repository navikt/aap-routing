package no.nav.aap.fordeling.arkiv.fordeling

import no.nav.aap.api.felles.AktørId
import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.felles.error.IntegrationException
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.FIKTIVTFNR
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
           with(bruker) {
               val fnr = tilFnr(dto.journalpostId)
               Journalpost(tittel,
                       journalfoerendeEnhet,
                       journalpostId,
                       journalstatus,
                       tema.lowercase(),
                       behandlingstema,
                       fnr,
                       Bruker(fnr),
                       avsenderMottaker.tilAvsenderMottaker(dto.journalpostId),  // kab være annerledes, derfor nytt oppslag
                       kanal,
                       relevanteDatoer,
                       dokumenter.toSortedSet(compareBy(DokumentInfo::dokumentInfoId)),
                       tilleggsopplysninger)
           }
       }

    private fun AvsenderMottakerDTO.tilAvsenderMottaker(journalpostId: String) = AvsenderMottaker(tilFnr(journalpostId))
    private fun BrukerDTO.tilFnr(journalpostId: String) =
        with(this) {
            when(type) {
                AKTOERID -> tilFnr(AktørId(id),journalpostId)
                FNR -> Fødselsnummer(id)
                else -> FIKTIVTFNR.also {
                    log.warn("IdType $type ikke støttet, bruker fiktivt FNR for $this for journalpost $journalpostId")
                }
            }
        }

    private fun tilFnr(aktørId: AktørId,journalpostId: String) =
        pdl.fnr(aktørId) ?: throw IntegrationException("Kunne ikke slå opp FNR for aktørid $aktørId for journalpost $journalpostId")
}