package no.nav.aap.fordeling.arkiv.journalpost

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import no.nav.aap.api.felles.AktørId
import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.felles.error.IrrecoverableIntegrationException
import no.nav.aap.fordeling.fordeling.Fordeler.Companion.FIKTIVTFNR
import no.nav.aap.fordeling.fordeling.FordelingDTOs.JournalpostDTO
import no.nav.aap.fordeling.fordeling.FordelingDTOs.JournalpostDTO.BrukerDTO
import no.nav.aap.fordeling.fordeling.FordelingDTOs.JournalpostDTO.DokumentInfoDTO
import no.nav.aap.fordeling.fordeling.FordelingDTOs.JournalpostDTO.IDTypeDTO
import no.nav.aap.fordeling.fordeling.FordelingDTOs.JournalpostDTO.IDTypeDTO.AKTOERID
import no.nav.aap.fordeling.fordeling.FordelingDTOs.JournalpostDTO.IDTypeDTO.FNR
import no.nav.aap.fordeling.fordeling.FordelingDTOs.JournalpostDTO.JournalStatusDTO
import no.nav.aap.fordeling.fordeling.FordelingDTOs.JournalpostDTO.JournalStatusDTO.JOURNALFOERT
import no.nav.aap.fordeling.fordeling.FordelingDTOs.JournalpostDTO.JournalStatusDTO.MOTTATT
import no.nav.aap.fordeling.arkiv.journalpost.Journalpost.Bruker
import no.nav.aap.fordeling.arkiv.journalpost.Journalpost.DokumentInfo
import no.nav.aap.fordeling.arkiv.journalpost.Journalpost.JournalpostStatus
import no.nav.aap.fordeling.arkiv.journalpost.Journalpost.Tilleggsopplysning
import no.nav.aap.fordeling.egenansatt.EgenAnsattClient
import no.nav.aap.fordeling.fordeling.AvsenderMottaker
import no.nav.aap.fordeling.navenhet.NAVEnhet
import no.nav.aap.fordeling.navenhet.NAVEnhet.Companion.VIKAFOSSEN
import no.nav.aap.fordeling.person.PDLClient
import no.nav.aap.util.ExtensionUtils.mapToSet
import no.nav.aap.util.StringExtensions.toUUID

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
                eksternReferanseId.toUUID(),
                dokumenter.toDomain(),
                tilleggsopplysninger.mapToSet { (k, v) -> Tilleggsopplysning(k, v) }).let {
                if (it.tilVikafossen) {
                    it.copy(enhet = VIKAFOSSEN)
                }
                else it
            }
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
            map { (id, tittel, kode, varianter) ->
                DokumentInfo(id, tittel, kode, varianter.map { it.variantFormat })
            }.toSortedSet(compareBy(DokumentInfo::id))
    }
}