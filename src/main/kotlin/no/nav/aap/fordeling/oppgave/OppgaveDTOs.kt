package no.nav.aap.fordeling.oppgave

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.time.LocalDate
import java.time.LocalDateTime.now
import java.time.ZoneId.systemDefault
import java.util.Date
import no.bekk.bekkopen.date.NorwegianDateUtil.addWorkingDaysToDate
import no.nav.aap.fordeling.arkiv.fordeling.Journalpost
import no.nav.aap.fordeling.navenhet.NAVEnhet.Companion.AUTO_ENHET

object OppgaveDTOs {

    data class OppgaveRespons(val antallTreffTotalt : Int, val oppgaver : List<Oppgave>) {
        @JsonIgnoreProperties(ignoreUnknown = true)
        data class Oppgave(val id : Long)
    }
}

fun Journalpost.opprettOppgaveData(oppgaveType : OppgaveType, tema : String, enhetNr : String? = null) =
    OpprettOppgaveData(fnr.fnr, id, behandlingstema, enhetNr, tittel, oppgaveType.verdi, tema.uppercase())

enum class OppgaveType(val verdi : String) { JOURNALFØRINGSOPPGAVE("JFR"), FORDELINGSOPPGAVE("FDR") }

data class OpprettOppgaveData(
    val personident : String,
    val journalpostId : String,
    val behandlingstema : String?,
    val tildeltEnhetsnr : String?,
    val beskrivelse : String?,
    val oppgavetype : String,
    val tema : String,
    val behandlingstype : String? = null,
    val prioritet : String = NORMAL_PRIORITET,
    val fristFerdigstillelse : LocalDate = frist,
    val aktivDato : LocalDate = LocalDate.now(),
    val opprettetAvEnhetsnr : String = AUTO_ENHET) {

    companion object {

        private const val NORMAL_PRIORITET = "NORM"
        private const val SISTE_ARBEIDSTIME = 12

        private fun Int.dagerTilFrist() = if (this < SISTE_ARBEIDSTIME) 1 else 2
        private val frist =
            with(now()) {
                addWorkingDaysToDate(Date.from(toLocalDate().atStartOfDay(systemDefault()).toInstant()),
                    hour.dagerTilFrist()).toInstant()
                    .atZone(systemDefault()).toLocalDate()
            }
    }
}