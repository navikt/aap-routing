package no.nav.aap.fordeling.arkiv.fordeling

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import no.nav.aap.api.felles.AktørId
import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.felles.error.IrrecoverableIntegrationException
import no.nav.aap.fordeling.arkiv.fordeling.Fordeler.Companion.FIKTIVTFNR
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.JournalpostDTO
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.JournalpostDTO.BrukerDTO
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.JournalpostDTO.BrukerDTO.BrukerTypeDTO.AKTOERID
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.JournalpostDTO.BrukerDTO.BrukerTypeDTO.FNR
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.JournalpostDTO.DokumentInfoDTO
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.JournalpostDTO.JournalStatusDTO
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.JournalpostDTO.JournalStatusDTO.JOURNALFOERT
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.JournalpostDTO.JournalStatusDTO.MOTTATT
import no.nav.aap.fordeling.arkiv.fordeling.Journalpost.Bruker
import no.nav.aap.fordeling.arkiv.fordeling.Journalpost.DokumentInfo
import no.nav.aap.fordeling.arkiv.fordeling.Journalpost.JournalpostStatus
import no.nav.aap.fordeling.egenansatt.EgenAnsattClient
import no.nav.aap.fordeling.navenhet.NAVEnhet
import no.nav.aap.fordeling.person.PDLClient

@Component
class JournalpostMapper(private val pdl : PDLClient, private val egen : EgenAnsattClient) {

    private val log = LoggerFactory.getLogger(JournalpostMapper::class.java)

    fun tilJournalpost(dto : JournalpostDTO) =
        with(dto) {
            val brukerFnr = bruker.fnr(journalpostId, "'bruker'")
            val avsenderMottakerFnr = avsenderMottaker.fnr(journalpostId, "'avsenderMottaker'")
            Journalpost(journalpostId,
                journalstatus.toDomain(),
                journalfoerendeEnhet?.let(::NAVEnhet),
                tittel,
                tema.lowercase(),
                behandlingstema,
                brukerFnr ?: FIKTIVTFNR,
                brukerFnr?.let { Bruker(it, pdl.diskresjonskode(it), egen.erEgenAnsatt(it)) },
                avsenderMottakerFnr?.let(::AvsenderMottaker),
                kanal,
                dokumenter.toDomain())
        }

    private fun JournalStatusDTO.toDomain() =
        when (this) {
            MOTTATT -> JournalpostStatus.MOTTATT
            JOURNALFOERT -> JournalpostStatus.JOURNALFØRT
            else -> JournalpostStatus.UKJENT
        }

    private fun BrukerDTO?.fnr(journalpostId : String, kind : String) =
        this?.let {
            when (idType) {
                AKTOERID -> AktørId(id).fnr(journalpostId)
                FNR -> Fødselsnummer(id)
                else -> null.also {
                    log.warn("IdType $idType ikke støttet for $kind med id $id fra journalpost $journalpostId")
                }
            }
        }

    private fun AktørId.fnr(journalpostId : String) =
        pdl.fnr(this) ?: throw IrrecoverableIntegrationException("Kunne ikke slå opp FNR for aktørid $this i journalpost $journalpostId")

    override fun toString() = "JournalpostMapper(pdl=$pdl, egen=$egen)"

    companion object {

        fun Bruker.toDTO() = BrukerDTO(fnr.fnr, FNR)

        fun Set<DokumentInfoDTO>.toDomain() =
            map { (dokumentInfoId, tittel, brevkode) -> DokumentInfo(dokumentInfoId, tittel, brevkode) }
                .toSortedSet(compareBy(DokumentInfo::id))
    }
}