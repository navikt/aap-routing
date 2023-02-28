package no.nav.aap.fordeling

import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.fordeling.arena.ArenaDTOs.ArenaOpprettOppgaveData
import no.nav.aap.fordeling.arena.ArenaWebClientAdapter
import no.nav.aap.fordeling.arkiv.ArkivWebClientAdapter
import no.nav.aap.fordeling.arkiv.JournalpostDTO.OppdaterForespørsel
import no.nav.aap.fordeling.egenansatt.EgenAnsattClient
import no.nav.aap.fordeling.navorganisasjon.NavOrgWebClientAdapter
import no.nav.aap.fordeling.oppgave.OppgaveClient
import no.nav.aap.fordeling.person.PDLWebClientAdapter
import no.nav.aap.util.LoggerUtil.getLogger
import no.nav.security.token.support.spring.UnprotectedRestController
import org.springframework.http.MediaType.TEXT_PLAIN_VALUE
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam

@UnprotectedRestController(value = ["/dev"])
class DevController(private val pdl: PDLWebClientAdapter, private val egen: EgenAnsattClient, private val arkiv: ArkivWebClientAdapter, private val oppgave: OppgaveClient, private val arena: ArenaWebClientAdapter, private val org: NavOrgWebClientAdapter) {

    private val log = getLogger(javaClass)
    @PostMapping("oppdaterogferdigstillJournalpost")
    fun oppdaterOgFerdigstillJournalpost(@RequestBody  data: OppdaterForespørsel, @RequestParam journalpostId: String) = arkiv.oppdaterOgFerdigstillJournalpost(journalpostId,data)

    @PostMapping("oppdater")
    fun oppdaterJournalpost( @RequestParam journalpostId: String,@RequestBody  data: OppdaterForespørsel) = arkiv.oppdaterJournalpost(journalpostId,data)
    @PostMapping("ferdigstilljournalpost", produces = [TEXT_PLAIN_VALUE])
    fun ferdigstillJournalpost( @RequestParam journalpostId: String) =
         arkiv.ferdigstillJournalpost(journalpostId)

    @GetMapping("hargosysoppgave")
    fun gosysHarOppgave(@RequestParam journalpostId: String) = oppgave.harOppgave(journalpostId)

    @GetMapping("nyestearenasak")
    fun nyesteArenaSak(@RequestParam fnr: Fødselsnummer) = arena.nyesteArenaSak(fnr)

    @GetMapping("skjerming")
    fun erSkjermet(@RequestParam fnr: Fødselsnummer) =  egen.erSkjermet(fnr)

    @GetMapping("aktiveenheter")
    fun aktiveEnheter() = org.aktiveEnheter()

    @GetMapping("diskresjonskode")
    fun diskresjonskode(@RequestParam fnr: Fødselsnummer) = pdl.diskresjonskode(fnr)

    @GetMapping("gt")
    fun gt(@RequestParam fnr: Fødselsnummer) = pdl.geoTilknytning(fnr)

    @PostMapping("opprettarenaoppgave")
    fun arenaOpprettOppgave(@RequestBody  data: ArenaOpprettOppgaveData) = arena.opprettArenaOppgave(data)

}