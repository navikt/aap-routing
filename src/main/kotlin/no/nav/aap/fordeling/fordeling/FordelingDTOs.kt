package no.nav.aap.fordeling.fordeling

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonEnumDefaultValue

object FordelingDTOs {

    data class JournalpostDTO(
        val tittel : String?,
        val journalfoerendeEnhet : String?,
        val journalpostId : String,
        val journalstatus : JournalStatusDTO,
        val tema : String,
        val behandlingstema : String?,
        val bruker : BrukerDTO?,
        val avsenderMottaker : AvsenderMottakerDTO?,
        val kanal : Kanal,
        val dokumenter : Set<DokumentInfoDTO>,
        val eksternReferanseId : String?,
        val tilleggsopplysninger : Set<TilleggsopplysningDTO> = emptySet()) {

        enum class Kanal { NAV_NO, EESSI, NAV_NO_CHAT, EKST_OPPS, SKAN_IM,

            @JsonEnumDefaultValue
            UKJENT
        }

        data class TilleggsopplysningDTO(val nokkel : String, val verdi : String)
        enum class JournalStatusDTO { MOTTATT, JOURNALFOERT,

            @JsonEnumDefaultValue
            UKJENT
        }

        data class OppdateringDataDTO(
            val tittel : String?,
            val avsenderMottaker : BrukerDTO?,
            val bruker : BrukerDTO?,
            val sak : SakDTO? = null,
            val tema : String) {

            data class SakDTO(val fagsakId : String, val sakstype : String = FAGSAK, val fagsaksystem : String = FAGSAKSYSTEM) {
                companion object {

                    private const val FAGSAK = "FAGSAK"
                    private const val FAGSAKSYSTEM = "AO01"
                }
            }
        }

        data class OppdateringResponsDTO(val journalpostId : String) {
            companion object {

                val EMPTY = OppdateringResponsDTO("0")
            }
        }

        data class DokumentInfoDTO(val dokumentInfoId : String, val tittel : String?, val brevkode : String?, val dokumentVarianter : List<DokumentVariant>)

        enum class IDTypeDTO { FNR, AKTOERID,

            @JsonEnumDefaultValue
            UKJENT
        }

        data class BrukerDTO(val id : String, @JsonAlias("type") val idType : IDTypeDTO)
        data class AvsenderMottakerDTO(val id : String?, @JsonAlias("type") val idType : IDTypeDTO?)
    }

    data class DokumentVariant(val variantFormat : VariantFormat) {
        enum class VariantFormat { ORIGINAL, ARKIV,

            @JsonEnumDefaultValue
            NA
        }
    }
}