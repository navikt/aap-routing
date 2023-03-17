package no.nav.aap.fordeling.arkiv.fordeling

import no.nav.aap.api.felles.AktørId
import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.felles.error.IntegrationException
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

    private val FIKTIVTFNR = Fødselsnummer("08089403198")  // Fiktivt i tilfelle du lurte


    fun tilJournalpost(dto: JournalpostDTO) =
        with(dto) {
            val brukerFnr = bruker?.tilFnr(journalpostId,"'bruker'")
            val avsenderMottakerFnr = avsenderMottaker?.tilFnr(journalpostId,"'avsenderMottaker'")
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

    private fun BrukerDTO.tilFnr(journalpostId: String, kind: String, defaultValue: Fødselsnummer? = null) =
        with(this) {
            when(type) {
                AKTOERID -> id?.let { tilFnr(AktørId(it),journalpostId) }
                FNR -> id?.let { Fødselsnummer(it) }
                else -> defaultValue.also {
                    log.warn("IdType $type ikke støttet, bruker default verdi $defaultValue for $kind med id $id, type $type i journalpost $journalpostId")
                }
            }
        }

    private fun tilFnr(aktørId: AktørId,journalpostId: String) =
        pdl.fnr(aktørId) ?: throw IntegrationException("Kunne ikke slå opp FNR for aktørid $aktørId i journalpost $journalpostId")
}