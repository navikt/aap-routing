package no.nav.aap.fordeling

import no.nav.aap.fordeling.arkiv.ArkivWebClientAdapter
import no.nav.aap.fordeling.arkiv.Integrasjoner
import no.nav.aap.fordeling.arkiv.JournalpostDTO.OppdaterJournalpostForespørsel
import no.nav.security.token.support.spring.UnprotectedRestController
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam

@UnprotectedRestController(value = ["/dev"])
class DevController(private val arkiv: ArkivWebClientAdapter, private val integrassjoner: Integrasjoner) {
    @PostMapping("oppdaterogferdigstill")
    fun oppdaterOgFerdigstill(@RequestBody  data: OppdaterJournalpostForespørsel, @RequestParam id: String) = arkiv.oppdaterOgFerdigstillJournalpost(id,data)

    @GetMapping("haroppgave")
    fun harOppgave(@RequestParam id: String) = integrassjoner.oppgave.harOppgave(id)
}