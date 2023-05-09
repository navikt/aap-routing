package no.nav.aap.fordeling

import org.springframework.http.MediaType.TEXT_PLAIN_VALUE
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import no.nav.aap.api.felles.AktørId
import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.fordeling.arena.ArenaWebClientAdapter
import no.nav.aap.fordeling.arkiv.ArkivClient
import no.nav.aap.fordeling.arkiv.dokarkiv.DokarkivWebClientAdapter
import no.nav.aap.fordeling.arkiv.journalpost.Journalpost
import no.nav.aap.fordeling.arkiv.saf.SAFGraphQLAdapter
import no.nav.aap.fordeling.egenansatt.EgenAnsattClient
import no.nav.aap.fordeling.navenhet.NAVEnhet
import no.nav.aap.fordeling.navenhet.NavEnhetClient
import no.nav.aap.fordeling.oppgave.OppgaveClient
import no.nav.aap.fordeling.person.Diskresjonskode
import no.nav.aap.fordeling.person.PDLWebClientAdapter
import no.nav.aap.util.Constants.AAP
import no.nav.aap.util.LoggerUtil.getLogger
import no.nav.security.token.support.spring.UnprotectedRestController

@UnprotectedRestController(value = ["/dev"])
class TestController(
    private val pdlAdapter : PDLWebClientAdapter,
    private val egenClient : EgenAnsattClient,
    private val arkivAdapter : DokarkivWebClientAdapter,
    private val arkivClient : ArkivClient,
    private val oppgaveClient : OppgaveClient,
    private val arenaAdapter : ArenaWebClientAdapter,
    private val safAdapter : SAFGraphQLAdapter,
    private val orgClient : NavEnhetClient) {

    private val log = getLogger(javaClass)

    @GetMapping("safjournalpost")
    fun safjournalpost(@RequestParam journalpostId : String) = safAdapter.hentJournalpostRAW(journalpostId)

    @PostMapping("ferdigstilljournalpost", produces = [TEXT_PLAIN_VALUE])
    fun ferdigstillJournalpost(@RequestParam journalpostId : String) =
        arkivAdapter.ferdigstillJournalpost(journalpostId)

    @GetMapping("journalpost")
    fun journalpost(@RequestParam journalpostId : String) = arkivClient.hentJournalpost(journalpostId)

    @GetMapping("hargosysoppgave")
    fun gosysHarOppgave(@RequestParam journalpostId : String) = oppgaveClient.harOppgave(journalpostId)

    @GetMapping("nyestearenasak")
    fun nyesteArenaSak(@RequestParam fnr : Fødselsnummer) = arenaAdapter.nyesteArenaSak(fnr)

    @GetMapping("skjerming")
    fun erEgenAnsatt(@RequestParam fnr : Fødselsnummer) = egenClient.erEgenAnsatt(fnr)

    @GetMapping("aktiveenheter")
    fun aktiveEnheter() = orgClient.aktiveEnheter()

    @GetMapping("eraktiv")
    fun erAktiv(@RequestParam enhetNr : String) = orgClient.erAktiv(NAVEnhet(enhetNr), aktiveEnheter())

    @GetMapping("enhet")
    fun enhet(@RequestParam område : String, @RequestParam skjermet : Boolean, @RequestParam diskresjonekode : Diskresjonskode) =
        orgClient.navEnhet(område, skjermet, diskresjonekode, AAP.uppercase())

    @GetMapping("diskresjonskode")
    fun diskresjonskode(@RequestParam fnr : Fødselsnummer) = pdlAdapter.diskresjonskode(fnr)

    @GetMapping("gt")
    fun gt(@RequestParam fnr : Fødselsnummer) = pdlAdapter.geoTilknytning(fnr)

    @GetMapping("fnr")
    fun fnr(@RequestParam aktørId : AktørId) = pdlAdapter.fnr(aktørId)

    @PostMapping("opprettarenaoppgave")
    fun arenaOpprettOppgave(@RequestBody jp : Journalpost) = arenaAdapter.opprettArenaOppgave(jp, "666")
}