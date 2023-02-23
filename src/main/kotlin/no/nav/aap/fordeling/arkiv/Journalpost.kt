package no.nav.aap.fordeling.arkiv

import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.fordeling.arkiv.JournalpostDTO.Bruker
import no.nav.aap.fordeling.arkiv.JournalpostDTO.DokumentInfo
import no.nav.aap.fordeling.arkiv.JournalpostDTO.JournalStatus
import no.nav.aap.fordeling.arkiv.JournalpostDTO.OppdaterJournalpostForespørsel
import no.nav.aap.fordeling.arkiv.JournalpostDTO.RelevantDato

data class Journalpost(val tittel: String?, val journalførendeEnhet: String?, val journalpostId: String, val status: JournalStatus,
                       val tema: String, val behandlingstema: String?, val fnr: Fødselsnummer, val bruker: Bruker?, val avsenderMottager: Bruker,
                       val relevanteDatoer: Set<RelevantDato>, val dokumenter: Set<DokumentInfo>) {

    fun oppdateringsData(saksNr: String) = OppdaterJournalpostForespørsel(tittel, avsenderMottager)

    val hovedDokumentBrevkode = dokumenter.firstOrNull()?.brevkode ?: "Brevkode ikke satt"
}