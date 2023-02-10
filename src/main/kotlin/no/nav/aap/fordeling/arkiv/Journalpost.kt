package no.nav.aap.fordeling.arkiv

import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.fordeling.arkiv.JournalpostDTO.DokumentInfo
import no.nav.aap.fordeling.arkiv.JournalpostDTO.JournalStatus
import no.nav.aap.fordeling.arkiv.JournalpostDTO.RelevantDato

data class Journalpost(val tittel: String?, val journalførendeEnhet: String?, val journalpostId: String, val status: JournalStatus,
                       val tema: String, val behandlingstema: String?, val fnr: Fødselsnummer,
                       val relevanteDatoer: Set<RelevantDato>, val dokumenter: Set<DokumentInfo>)