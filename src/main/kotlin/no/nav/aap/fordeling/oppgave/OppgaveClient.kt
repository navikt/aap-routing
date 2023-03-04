package no.nav.aap.fordeling.oppgave

import no.nav.aap.fordeling.arkiv.fordeling.Journalpost
import no.nav.aap.fordeling.navorganisasjon.EnhetsKriteria.NAVEnhet
import no.nav.aap.fordeling.oppgave.OppgaveType.FORDELINGSOPPGAVE
import no.nav.aap.fordeling.oppgave.OppgaveType.JOURNALFØRINGSOPPGAVE
import org.springframework.stereotype.Component

@Component
class OppgaveClient(private val a: OppgaveWebClientAdapter) {
    fun harOppgave(journalpostId: String) = a.harOppgave(journalpostId)
    fun opprettManuellJournalføringOppgave(journalpost: Journalpost, navEnhet: NAVEnhet) = a.opprettOppgave(journalpost.tilOpprettOppgave(JOURNALFØRINGSOPPGAVE,navEnhet.enhetNr))
    fun opprettFordelingOppgave(journalpost: Journalpost) = a.opprettOppgave(journalpost.tilOpprettOppgave(FORDELINGSOPPGAVE))

}