package no.nav.aap.fordeling.arkiv.fordeling

import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.fordeling.arena.ArenaDTOs.ArenaOpprettOppgaveData
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.JournalpostDTO.Bruker
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.JournalpostDTO.DokumentInfo
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.JournalpostDTO.JournalStatus
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.JournalpostDTO.OppdateringData
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.JournalpostDTO.OppdateringData.Sak
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.JournalpostDTO.RelevantDato
import no.nav.aap.fordeling.navenhet.EnhetsKriteria.NavOrg.NAVEnhet

data class Journalpost(
        val tittel: String?,
        val journalførendeEnhet: String?,
        val journalpostId: String,
        val status: JournalStatus,
        val tema: String,
        val behandlingstema: String?,
        val fnr: Fødselsnummer,
        val bruker: Bruker?,
        val avsenderMottager: Bruker?,
        val kanal: String,
        val relevanteDatoer: Set<RelevantDato>,
        val dokumenter: Set<DokumentInfo>) {

    val hovedDokumentBrevkode = dokumenter.firstOrNull()?.brevkode ?: "Ukjent brevkode"

    val hovedDokumentTittel = dokumenter.firstOrNull()?.tittel ?: "Ukjent tittel"

    val vedleggTitler = dokumenter.drop(1).mapNotNull { it.tittel }

    fun opprettArenaOppgaveData(enhet: NAVEnhet) =
        ArenaOpprettOppgaveData(fnr, enhet.enhetNr, hovedDokumentTittel, vedleggTitler)

    fun oppdateringsData(saksNr: String) =
        OppdateringData(tittel, avsenderMottager ?: bruker, bruker, Sak(saksNr), tema.uppercase())

}