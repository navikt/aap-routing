package no.nav.aap.fordeling.arkiv

import java.time.LocalDateTime
import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.fordeling.arkiv.JournalpostDTO.BrukerDTO.BrukerType
import no.nav.aap.fordeling.arkiv.JournalpostDTO.BrukerDTO.BrukerType.FNR
import no.nav.aap.util.Constants

data class JournalpostDTO(
        val tittel: String?,
        val journalfoerendeEnhet: String?,
        val journalpostId: String,
        val journalstatus: JournalStatus,
        val tema: String,
        val behandlingstema: String?,
        val bruker: BrukerDTO,
        val avsenderMottaker: BrukerDTO,
        val relevanteDatoer: Set<RelevantDato>,
        val dokumenter: Set<DokumentInfo>) {


    fun tilJournalpost() =
        Journalpost(tittel,journalfoerendeEnhet,journalpostId,journalstatus,tema.lowercase(),behandlingstema,
                Fødselsnummer(bruker.id), Bruker(bruker.id,bruker.type),
                Bruker(avsenderMottaker.id,avsenderMottaker.type),relevanteDatoer,dokumenter)


    enum class JournalStatus {
        MOTTATT,
        JOURNALFOERT,
        EKSPEDERT,
        FERDIGSTILT,
        UNDER_ARBEID,
        FEILREGISTRERT,
        UTGAAR,
        AVBRUTT,
        UKJENT_BRUKER,
        RESERVERT,
        OPPLASTING_DOKUMENT,
        UKJENT
    }
    data class RelevantDato(val dato: LocalDateTime, val datotype: RelevantDatoType) {

        enum class RelevantDatoType {
            DATO_OPPRETTET,
            DATO_SENDT_PRINT,
            DATO_EKSPEDERT,
            DATO_JOURNALFOERT,
            DATO_REGISTRERT,
            DATO_AVS_RETUR,
            DATO_DOKUMENT
        }
    }

    data class OppdaterForespørsel(val tittel: String?, val avsenderMottaker: Bruker?, val bruker: Bruker?, val sak: Sak, val tema: String = Constants.AAP.uppercase()) {
        data class Sak(val fagsakId: String, val sakstype: String = FAGSAK, val fagsaksystem: String = FAGSAKSYSTEM)
    }

    data class OppdaterRespons(val journalpostId: String)

    data class Bruker(val id: String, val idType: BrukerType = FNR)

    data class DokumentInfo(val dokumentInfoId: String, val tittel: String?,val brevkode: String?)

    data class BrukerDTO(val id: String, val type: BrukerType) {
        enum class BrukerType {
            FNR,
            AKTOERID,
            ORGNR,
            HPRNR,
            UTL_ORG,
            NULL,
            UKJENT,
        }
    }

    companion object  {
        private const val FAGSAK = "FAGSAK"
        private const val FAGSAKSYSTEM = "AO01"
    }
}