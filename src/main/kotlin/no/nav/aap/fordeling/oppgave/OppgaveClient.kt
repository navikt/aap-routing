package no.nav.aap.fordeling.oppgave

import org.springframework.stereotype.Component
import no.nav.aap.fordeling.arkiv.fordeling.Journalpost
import no.nav.aap.fordeling.navenhet.NAVEnhet
import no.nav.aap.fordeling.oppgave.OppgaveType.FORDELINGSOPPGAVE
import no.nav.aap.fordeling.oppgave.OppgaveType.JOURNALFØRINGSOPPGAVE

@Component
class OppgaveClient(private val adapter : OppgaveWebClientAdapter) {

    fun harOppgave(journalpostId : String) = adapter.harOppgave(journalpostId)

    fun opprettJournalføringOppgave(jp : Journalpost, navEnhet : NAVEnhet) =
        adapter.opprettOppgave(jp.opprettOppgaveData(JOURNALFØRINGSOPPGAVE, jp.tema, navEnhet.enhetNr))

    fun opprettFordelingOppgave(jp : Journalpost) = adapter.opprettOppgave(jp.opprettOppgaveData(FORDELINGSOPPGAVE, jp.tema))

    override fun toString() = "OppgaveClient(adapter=$adapter)"
}