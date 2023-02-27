package no.nav.aap.fordeling

import no.nav.aap.fordeling.arkiv.ArkivWebClientAdapter
import no.nav.aap.fordeling.arkiv.JournalpostDTO.OppdaterForespørsel
import no.nav.aap.util.LoggerUtil
import no.nav.security.token.support.spring.UnprotectedRestController
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam

@UnprotectedRestController(value = ["/dev"])
class DevController(private val arkiv: ArkivWebClientAdapter, private val integrassjoner: Integrasjoner) {

    private val log = LoggerUtil.getLogger(javaClass)
    @PostMapping("oppdaterogferdigstill")
    fun oppdaterOgFerdigstill(@RequestBody  data: OppdaterForespørsel, @RequestParam journalpostId: String) = arkiv.oppdaterOgFerdigstillJournalpost(journalpostId,data)

    @PostMapping("oppdater")
    fun oppdater( @RequestParam journalpostId: String,@RequestBody  data: OppdaterForespørsel) = arkiv.oppdaterJournalpost(journalpostId,data)

    @PostMapping("ferdigstill")
    fun ferdigstill( @RequestParam journalpostId: String): Any? {
        kotlin.runCatching {
            log.info("Ferdigstilling journalpost $journalpostId")
            return arkiv.ferdigstillJournalpost(journalpostId)
        }.getOrElse{
            log.info("Ferdigstilling feilet")
            throw it
        }

    @GetMapping("haroppgave")
    fun harOppgave(@RequestParam journalpostId: String) = integrassjoner.oppgave.harOppgave(journalpostId)
}