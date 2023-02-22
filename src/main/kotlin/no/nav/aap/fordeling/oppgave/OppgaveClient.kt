package no.nav.aap.fordeling.oppgave

import no.nav.aap.fordeling.arkiv.Journalpost
import no.nav.aap.fordeling.navorganisasjon.NavEnhet
import org.springframework.stereotype.Component

@Component
class OppgaveClient(private val a: OppgaveWebClientAdapter) {
    fun harOppgave(journalpostId: String) = a.harOppgave(journalpostId)
    fun opprettManuellJournalføringOppgave(journalpost: Journalpost, navEnhet: NavEnhet) = a.opprettManuellJournalføringsOppgave(journalpost.tilOpprettOppgave(navEnhet.enhetNr))

}