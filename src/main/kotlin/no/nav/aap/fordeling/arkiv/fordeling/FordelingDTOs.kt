package no.nav.aap.fordeling.arkiv.fordeling

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonEnumDefaultValue
import java.time.LocalDateTime
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.FordelingResultat.FordelingType.INGEN
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.JournalpostDTO.BrukerDTO

typealias AvsenderMottakerDTO = BrukerDTO

object FordelingDTOs {

    data class FordelingResultat(val fordelingstype : FordelingType, val msg : String, val brevkode : String, val journalpostId : String = "0") {

        fun msg() = "$fordelingstype: $msg for journalpost $journalpostId ($brevkode)"
        enum class FordelingType {
            AUTOMATISK,
            MANUELL_JOURNALFØRING,
            MANUELL_FORDELING,
            INGEN,
            ALLEREDE_OPPGAVE,
            ALLEREDE_JOURNALFØRT,
            INGEN_JOURNALPOST,
            DIREKTE_MANUELL,
            FAILED
        }

        companion object {

            val INGEN_FORDELING = FordelingResultat(INGEN, "Ingen fordeling utført", "Ingen brevkode")
        }
    }

    data class JournalpostDTO(
        val tittel : String?,
        val journalfoerendeEnhet : String?,
        val journalpostId : String,
        val journalstatus : JournalStatusDTO,
        val journalpostType : JournalpostTypeDTO,
        val tema : String,
        val behandlingstema : String?,
        val bruker : BrukerDTO?,
        val avsenderMottaker : AvsenderMottakerDTO?,
        val kanal : Kanal,
        val relevanteDatoer : Set<RelevantDatoDTO>,
        val dokumenter : Set<DokumentInfoDTO>,
        val tilleggsopplysninger : Set<TilleggsopplysningDTO> = emptySet()) {

        enum class Kanal { NAV_NO, EESSI, NAV_NO_CHAT, EKST_OPPS, SKAN_IM,

            @JsonEnumDefaultValue
            UKJENT
        }

        enum class JournalpostTypeDTO { I, U, N }

        data class TilleggsopplysningDTO(val nokkel : String, val verdi : String)
        enum class JournalStatusDTO { MOTTATT, JOURNALFOERT,

            @JsonEnumDefaultValue
            UKJENT
        }

        data class RelevantDatoDTO(val dato : LocalDateTime, val datotype : RelevantDatoTypeDTO) {

            enum class RelevantDatoTypeDTO {
                DATO_OPPRETTET,
                DATO_SENDT_PRINT,
                DATO_EKSPEDERT,
                DATO_JOURNALFOERT,
                DATO_REGISTRERT,
                DATO_AVS_RETUR,
                DATO_DOKUMENT
            }
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

        data class DokumentInfoDTO(val dokumentInfoId : String, val tittel : String?, val brevkode : String?)

        data class BrukerDTO(val id : String?, @JsonAlias("type") val idType : BrukerTypeDTO?) {
            enum class BrukerTypeDTO {
                FNR,
                AKTOERID,
                ORGNR,
                HPRNR,
                UTL_ORG,
                NULL,
                UKJENT,
            }
        }
    }
}