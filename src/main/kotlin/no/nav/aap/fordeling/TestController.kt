package no.nav.aap.fordeling

import no.nav.aap.api.felles.AktørId
import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.fordeling.arena.ArenaDTOs.ArenaOpprettOppgaveData
import no.nav.aap.fordeling.arena.ArenaWebClientAdapter
import no.nav.aap.fordeling.arkiv.ArkivClient
import no.nav.aap.fordeling.arkiv.dokarkiv.DokarkivWebClientAdapter
import no.nav.aap.fordeling.arkiv.fordeling.AAPFordeler
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.JournalpostDTO.OppdateringData
import no.nav.aap.fordeling.arkiv.fordeling.Journalpost
import no.nav.aap.fordeling.egenansatt.EgenAnsattClient
import no.nav.aap.fordeling.navenhet.EnhetsKriteria.NavOrg.NAVEnhet
import no.nav.aap.fordeling.navenhet.NavEnhetClient
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
class TestController(
        private val fordeler: AAPFordeler,
        private val pdlAdapter: PDLWebClientAdapter,
        private val egenClient: EgenAnsattClient,
        private val arkivAdapter: DokarkivWebClientAdapter,
        private val arkivClient: ArkivClient,
        private val oppgaveClient: OppgaveClient,
        private val arenaAdapter: ArenaWebClientAdapter,
        private val orgClient: NavEnhetClient) {

    private val log = getLogger(javaClass)

    @PostMapping("oppdaterogferdigstilljournalpost")
    fun oppdaterOgFerdigstillJournalpost(@RequestBody data: OppdateringData, @RequestParam journalpostId: String) =
        arkivAdapter.oppdaterOgFerdigstillJournalpost(journalpostId, data)

    @PostMapping("oppdaterjournalpost")
    fun oppdaterJournalpost(@RequestParam journalpostId: String, @RequestBody data: OppdateringData) =
        arkivAdapter.oppdaterJournalpost(journalpostId, data)

    @PostMapping("ferdigstilljournalpost", produces = [TEXT_PLAIN_VALUE])
    fun ferdigstillJournalpost(@RequestParam journalpostId: String) =
        arkivAdapter.ferdigstillJournalpost(journalpostId)

    @GetMapping("journalpost")
    fun journalpost(@RequestParam journalpostId: String) = arkivClient.hentJournalpost(journalpostId)

    @PostMapping("fordel")
    fun fordelSøknad(@RequestBody journalpost: Journalpost, @RequestParam enhetNr: String) =
        fordeler.fordel(journalpost, NAVEnhet(enhetNr))

    @GetMapping("hargosysoppgave")
    fun gosysHarOppgave(@RequestParam journalpostId: String) = oppgaveClient.harOppgave(journalpostId)

    @GetMapping("nyestearenasak")
    fun nyesteArenaSak(@RequestParam fnr: Fødselsnummer) = arenaAdapter.nyesteArenaSak(fnr)

    @GetMapping("skjerming")
    fun erSkjermet(@RequestParam fnr: Fødselsnummer) = egenClient.erEgenAnsatt(fnr)

    @GetMapping("aktiveenheter")
    fun aktiveEnheter() = orgClient.aktiveEnheter()

    @GetMapping("eraktiv")
    fun erAktiv(@RequestParam enhetNr: String) = orgClient.erAktiv(enhetNr,aktiveEnheter())

    @GetMapping("diskresjonskode")
    fun diskresjonskode(@RequestParam fnr: Fødselsnummer) = pdlAdapter.diskresjonskode(fnr)

    @GetMapping("gt")
    fun gt(@RequestParam fnr: Fødselsnummer) = pdlAdapter.geoTilknytning(fnr)

    @GetMapping("fnr")
    fun fnr(@RequestParam aktørId: AktørId) = pdlAdapter.fnr(aktørId)


    @PostMapping("opprettarenaoppgave")
    fun arenaOpprettOppgave(@RequestBody data: ArenaOpprettOppgaveData) = arenaAdapter.opprettArenaOppgave(data)
}