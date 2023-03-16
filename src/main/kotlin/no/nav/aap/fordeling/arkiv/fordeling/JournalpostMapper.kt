package no.nav.aap.fordeling.arkiv.fordeling

import no.nav.aap.api.felles.AktørId
import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.felles.error.IntegrationException
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.FNRIKKESATT
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.JournalpostDTO
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.JournalpostDTO.Bruker
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.JournalpostDTO.BrukerDTO
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.JournalpostDTO.BrukerDTO.BrukerType.AKTOERID
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.JournalpostDTO.BrukerDTO.BrukerType.FNR
import no.nav.aap.fordeling.person.PDLClient
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class JournalpostMapper(private val pdl: PDLClient) {

    private val log = LoggerFactory.getLogger(JournalpostMapper::class.java)

    fun tilJournalpost(dto: JournalpostDTO) =
       with(dto) {
           with(bruker) {
               Journalpost(tittel,
                       journalfoerendeEnhet,
                       journalpostId,
                       journalstatus,
                       tema.lowercase(),
                       behandlingstema, fødselsnummer(this),
                       tilBruker(this),
                       tilBruker(avsenderMottaker),
                       kanal,
                       relevanteDatoer,
                       dokumenter.toSortedSet(compareBy{it.dokumentInfoId}),
                       tilleggsopplysninger)
           }
       }

    fun tilBruker(dto: BrukerDTO) = Bruker(dto.id)

    private fun fødselsnummer(dto: BrukerDTO) =
        with(dto) {
            when(type) {
                AKTOERID -> fødselsnummer(id)
                FNR -> Fødselsnummer(id)
                else -> {
                    log.warn("IdType $type ikke støttet, bruker fiktivt FNR")
                    FNRIKKESATT
                }
            }
        }

    private fun fødselsnummer(aktørId: String) =
        pdl.fnr(AktørId(aktørId)) ?: throw IntegrationException("Kunne ikke slå opp FNR for aktørid $aktørId")
}