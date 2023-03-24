package no.nav.aap.fordeling.oppgave

import no.nav.aap.fordeling.arkiv.fordeling.Journalpost
import no.nav.aap.fordeling.navenhet.EnhetsKriteria.NavOrg.NAVEnhet
import no.nav.aap.fordeling.oppgave.OppgaveType.FORDELINGSOPPGAVE
import no.nav.aap.fordeling.oppgave.OppgaveType.JOURNALFØRINGSOPPGAVE
import org.springframework.stereotype.Component

@Component
class OppgaveClient(private val a: OppgaveWebClientAdapter) {
    fun harOppgave(journalpostId: String) = a.harOppgave(journalpostId)
    fun opprettJournalføringOppgave(jp: Journalpost, navEnhet: NAVEnhet) =
        a.opprettOppgave(jp.opprettOppgaveData(JOURNALFØRINGSOPPGAVE, jp.tema,navEnhet.enhetNr))

    fun opprettFordelingOppgave(jp: Journalpost) =
        a.opprettOppgave(jp.opprettOppgaveData(FORDELINGSOPPGAVE,jp.tema))

    override fun toString(): String {
        return "OppgaveClient(a=$a)"
    }
}