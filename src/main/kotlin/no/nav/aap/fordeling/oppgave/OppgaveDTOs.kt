package no.nav.aap.fordeling.oppgave

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId.*
import java.util.*
import no.bekk.bekkopen.date.NorwegianDateUtil.*
import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.fordeling.arkiv.Journalpost
import no.nav.aap.fordeling.arkiv.JournalpostDTO.JournalførendeEnhet.Companion.AUTO_ENHET
import no.nav.aap.fordeling.oppgave.OppgaveConfig.Companion.JOURNALFØRINGSOPPGAVE
import no.nav.aap.util.Constants.AAP

object OppgaveDTOs {

    data class OppgaveRespons(val antallTreffTotalt: Int, val oppgaver: List<Oppgave>) {
        @JsonIgnoreProperties(ignoreUnknown = true)
        data  class Oppgave(val id: Long)
    }
}

fun Journalpost.tilOpprettOppgave(enhetNr: String) =
    OpprettOppgaveData(fnr,fnr.fnr,journalpostId, behandlingstema,"TODO", JOURNALFØRINGSOPPGAVE,enhetNr,"TODO")
data class OpprettOppgaveData(
        val id: Fødselsnummer,
        val aktoerId: String,
        val journalpostId: String,
        val behandlingstema: String?,
        val behandlingstype: String,
        val oppgavetype: String,
        val tildeltEnhetsnr: String,
        val beskrivelse: String,
        val tema: String = AAP.uppercase(),
        val prioritet: String = NORMAL_PRIORITET,
        val isPerson: Boolean = true,
        val fristFerdigstillelse: String = frist(),
        val aktivDato: String = LocalDate.now().toString(),
        val opprettetAvEnhetsnr: String = AUTO_ENHET) {

    companion object {
        private const val NORMAL_PRIORITET = "NORM"
        private const val SISTE_ARBEIDSTIME = 12
        private fun dagerTilFrist(time: Long) = if (time < SISTE_ARBEIDSTIME) 1 else 2

        private fun frist() =
            with(LocalDateTime.now()) {
                 addWorkingDaysToDate(Date.from(toLocalDate().atStartOfDay(systemDefault()).toInstant()),
                        dagerTilFrist(hour.toLong())).toInstant()
                    .atZone(systemDefault()).toLocalDate().toString()
            }
    }
}