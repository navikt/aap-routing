package no.nav.aap.fordeling.arena

import no.nav.aap.api.felles.Fødselsnummer

object ArenaDTOs {
    data class ArenaOpprettetOppgave(val oppgaveId : String = "0", val arenaSakId : String = "0") {
        companion object {
            val EMPTY = ArenaOpprettetOppgave()
        }
    }

    data class ArenaOpprettOppgaveData(
        val fnr : Fødselsnummer,
        val enhet : String,
        val tittel : String,
        val titler : List<String> = emptyList())
}