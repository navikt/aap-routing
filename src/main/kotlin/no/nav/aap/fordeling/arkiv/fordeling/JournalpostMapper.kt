package no.nav.aap.fordeling.arkiv.fordeling

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import no.nav.aap.api.felles.AktørId
import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.felles.error.IrrecoverableIntegrationException
import no.nav.aap.fordeling.arkiv.fordeling.Fordeler.Companion.FIKTIVTFNR
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.JournalpostDTO
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.JournalpostDTO.BrukerDTO
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.JournalpostDTO.DokumentInfoDTO
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.JournalpostDTO.IDTypeDTO
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.JournalpostDTO.IDTypeDTO.AKTOERID
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.JournalpostDTO.IDTypeDTO.FNR
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.JournalpostDTO.JournalStatusDTO
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.JournalpostDTO.JournalStatusDTO.JOURNALFOERT
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.JournalpostDTO.JournalStatusDTO.MOTTATT
import no.nav.aap.fordeling.arkiv.fordeling.Journalpost.Bruker
import no.nav.aap.fordeling.arkiv.fordeling.Journalpost.DokumentInfo
import no.nav.aap.fordeling.arkiv.fordeling.Journalpost.JournalpostStatus
import no.nav.aap.fordeling.arkiv.fordeling.Journalpost.Tilleggsopplysning
import no.nav.aap.fordeling.egenansatt.EgenAnsattClient
import no.nav.aap.fordeling.navenhet.NAVEnhet
import no.nav.aap.fordeling.person.PDLClient

@Component
class JournalpostMapper(private val pdl : PDLClient, private val egen : EgenAnsattClient) {

    private val log = LoggerFactory.getLogger(JournalpostMapper::class.java)

    fun tilJournalpost(dto : JournalpostDTO) =
        with(dto) {
            val brukerFnr = bruker?.id.fnr(bruker?.idType, "'bruker  for $journalpostId'")
            Journalpost(journalpostId,
                journalstatus.toDomain(),
                journalfoerendeEnhet?.let(::NAVEnhet),
                tittel,
                tema.lowercase(),
                behandlingstema,
                brukerFnr ?: FIKTIVTFNR,
                brukerFnr?.let { Bruker(it, pdl.diskresjonskode(it), egen.erEgenAnsatt(it)) },
                avsenderMottaker?.id.fnr(avsenderMottaker?.idType, "'avsenderMottaker for $journalpostId'")?.let(::AvsenderMottaker),
                kanal,
                dokumenter.toDomain(),
                tilleggsopplysninger.mapToSet { Tilleggsopplysning(it.nokkel, it.verdi) })
        }

    private inline fun <T, R> Iterable<T>.mapToSet(transform : (T) -> R) : Set<R> {
        return mapTo(HashSet(), transform)
    }

    private fun JournalStatusDTO.toDomain() =
        when (this) {
            MOTTATT -> JournalpostStatus.MOTTATT
            JOURNALFOERT -> JournalpostStatus.JOURNALFØRT
            else -> JournalpostStatus.UKJENT.also {
                log.warn("Ukjent journalpoststatus $this")
            }
        }

    private fun String?.fnr(idType : IDTypeDTO?, kind : String) =
        this?.let {
            when (idType) {
                AKTOERID -> AktørId(this).fnr()
                FNR -> Fødselsnummer(this)
                null -> null.also {
                    log.warn("Null idType for $kind med id $this")
                }

                else -> null.also {
                    log.warn("IdType $idType ikke støttet for $kind med id $this")
                }
            }
        }

    private fun AktørId.fnr() =
        pdl.fnr(this) ?: throw IrrecoverableIntegrationException("Kunne ikke slå opp FNR for aktørid $this")

    override fun toString() = "JournalpostMapper(pdl=$pdl, egen=$egen)"

    companion object {

        fun Bruker.toDTO() = BrukerDTO(fnr.fnr, FNR)

        fun Set<DokumentInfoDTO>.toDomain() =
            map { (dokumentInfoId, tittel, brevkode, dokumentVarianter) ->
                DokumentInfo(dokumentInfoId,
                    tittel,
                    brevkode, dokumentVarianter.map { it.variantFormat })
            }.toSortedSet(compareBy(DokumentInfo::id))
    }
}