package no.nav.aap.fordeling.arena
import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.fordeling.arena.ArenaConfig.Companion.PERSON
import no.nav.aap.util.Constants.AAP

object ArenaDTOs {
    data class ArenaOpprettetOppgave(val oppgaveId: String = "0", val arenaSakId: String = "0") {
        companion object {
            val EMPTY = ArenaOpprettetOppgave()
        }
    }

    data class ArenaOpprettOppgaveData(val fnr: Fødselsnummer, val enhet: String, val tittel: String, val titler: List<String> = emptyList())
}