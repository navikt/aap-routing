package no.nav.aap.fordeling

import no.nav.aap.fordeling.arkiv.ArkivWebClientAdapter
import no.nav.aap.fordeling.arkiv.JournalpostDTO.OppdaterJournalpostForespørsel
import no.nav.aap.fordeling.oppgave.OppgaveWebClientAdapter
import no.nav.aap.fordeling.oppgave.OpprettOppgaveData
import no.nav.aap.fordeling.person.PDLWebClientAdapter
import no.nav.security.token.support.spring.UnprotectedRestController
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam

@UnprotectedRestController(value = ["/dev"])
class DevController(private val arkiv: ArkivWebClientAdapter, private val oppgave: OppgaveWebClientAdapter,private val pdl: PDLWebClientAdapter) {
    @PostMapping("oppdaterogferdigstill")
    fun oppdaterOgFerdigstill(@RequestBody  data: OppdaterJournalpostForespørsel, @RequestParam journalpostId: String) = arkiv.oppdaterOgFerdigstillJournalpost(journalpostId,data)

    @GetMapping("haroppgave")
    fun harOppgave(@RequestParam journalpostId: String) = oppgave.harOppgave(journalpostId)

    @PostMapping("opprettpppgave")
    fun tilManuellJournalføring(@RequestBody data: OpprettOppgaveData) = oppgave.opprettOppgave(data)
}