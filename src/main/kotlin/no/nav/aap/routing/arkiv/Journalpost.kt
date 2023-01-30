package no.nav.aap.routing.arkiv

import java.time.LocalDateTime
import no.nav.aap.api.felles.Fødselsnummer

data class Journalpost(val tittel: String?,val journalfoerendeEnhet: String?, val journalpostId: String,val journalstatus: JournalStatus,
                       val tema: String,val behandlingstema: String?, val bruker: Bruker, val avsenderMottaker: Bruker,
                       val relevanteDatoer: Set<RelevantDato>, val dokumenter: Set<DokumentInfo>) {

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

    data class DokumentInfo(val dokumentInfoId: String, val tittel: String?,val brevkode: String?)

    data class Bruker(val id: String, val type: AvsenderMottakerType) {
        enum class AvsenderMottakerType {
            FNR,
            ORGNR,
            HPRNR,
            UTL_ORG,
            NULL,
            UKJENT,
        }
    }
}