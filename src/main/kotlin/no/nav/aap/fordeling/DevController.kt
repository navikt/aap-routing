package no.nav.aap.fordeling

import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.fordeling.arena.ArenaDTOs.ArenaOpprettOppgaveData
import no.nav.aap.fordeling.arena.ArenaWebClientAdapter
import no.nav.aap.fordeling.arkiv.dokarkiv.DokarkivWebClientAdapter
import no.nav.aap.fordeling.arkiv.fordeling.AAPFordeler
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.JournalpostDTO.OppdateringData
import no.nav.aap.fordeling.arkiv.fordeling.Journalpost
import no.nav.aap.fordeling.egenansatt.EgenAnsattClient
import no.nav.aap.fordeling.navenhet.EnhetsKriteria.NavOrg.NAVEnhet
import no.nav.aap.fordeling.navenhet.NavEnhetWebClientAdapter
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
class DevController(
        private val fordeler: AAPFordeler,
        private val pdl: PDLWebClientAdapter,
        private val egen: EgenAnsattClient,
        private val arkiv: DokarkivWebClientAdapter,
        private val oppgave: OppgaveClient,
        private val arena: ArenaWebClientAdapter,
        private val org: NavEnhetWebClientAdapter,
                   ) {

    private val log = getLogger(javaClass)

    @PostMapping("oppdaterogferdigstilljournalpost")
    fun oppdaterOgFerdigstillJournalpost(@RequestBody data: OppdateringData, @RequestParam journalpostId: String) =
        arkiv.oppdaterOgFerdigstillJournalpost(journalpostId, data)

    @PostMapping("oppdaterjournalpost")
    fun oppdaterJournalpost(@RequestParam journalpostId: String, @RequestBody data: OppdateringData) =
        arkiv.oppdaterJournalpost(journalpostId, data)

    @PostMapping("ferdigstilljournalpost", produces = [TEXT_PLAIN_VALUE])
    fun ferdigstillJournalpost(@RequestParam journalpostId: String) =
        arkiv.ferdigstillJournalpost(journalpostId)

    @PostMapping("fordel")
    fun fordelSøknad(@RequestBody journalpost: Journalpost, @RequestParam enhetNr: String) =
        fordeler.fordel(journalpost, NAVEnhet(enhetNr))

    @GetMapping("hargosysoppgave")
    fun gosysHarOppgave(@RequestParam journalpostId: String) = oppgave.harOppgave(journalpostId)

    @GetMapping("nyestearenasak")
    fun nyesteArenaSak(@RequestParam fnr: Fødselsnummer) = arena.nyesteArenaSak(fnr)

    @GetMapping("skjerming")
    fun erSkjermet(@RequestParam fnr: Fødselsnummer) = egen.erSkjermet(fnr)

    @GetMapping("aktiveenheter")
    fun aktiveEnheter() = org.aktiveEnheter()

    @GetMapping("diskresjonskode")
    fun diskresjonskode(@RequestParam fnr: Fødselsnummer) = pdl.diskresjonskode(fnr)

    @GetMapping("gt")
    fun gt(@RequestParam fnr: Fødselsnummer) = pdl.geoTilknytning(fnr)

    @PostMapping("opprettarenaoppgave")
    fun arenaOpprettOppgave(@RequestBody data: ArenaOpprettOppgaveData) = arena.opprettArenaOppgave(data)
}