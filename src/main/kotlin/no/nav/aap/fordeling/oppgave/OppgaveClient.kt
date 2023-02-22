package no.nav.aap.fordeling.oppgave

import org.springframework.stereotype.Component

@Component
class OppgaveClient(private val a: OppgaveWebClientAdapter) {
    fun harOppgave(journalpostId: String) = a.harOppgave(journalpostId)

}