package no.nav.aap.fordeling.oppgave

import io.micrometer.observation.annotation.Observed
import org.springframework.stereotype.Component
import no.nav.aap.fordeling.arkiv.journalpost.Journalpost
import no.nav.aap.fordeling.navenhet.NAVEnhet
import no.nav.aap.fordeling.oppgave.OppgaveType.FORDELINGSOPPGAVE
import no.nav.aap.fordeling.oppgave.OppgaveType.JOURNALFØRINGSOPPGAVE

@Component
@Observed
class OppgaveClient(private val adapter : OppgaveWebClientAdapter) {

    fun harOppgave(journalpostId : String) = adapter.harOppgave(journalpostId)

    fun opprettJournalføringOppgave(jp : Journalpost, enhet : NAVEnhet) = adapter.opprettOppgave(jp, JOURNALFØRINGSOPPGAVE, enhet.enhetNr)

    fun opprettFordelingOppgave(jp : Journalpost) = adapter.opprettOppgave(jp, FORDELINGSOPPGAVE)

    override fun toString() = "OppgaveClient(adapter=$adapter)"
}