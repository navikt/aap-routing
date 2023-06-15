package no.nav.aap.fordeling

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType.*
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClient.Builder
import org.springframework.web.util.UriBuilder
import no.nav.aap.api.felles.AktørId
import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.fordeling.arena.ArenaWebClientAdapter
import no.nav.aap.fordeling.arkiv.ArkivClient
import no.nav.aap.fordeling.arkiv.dokarkiv.DokarkivWebClientAdapter
import no.nav.aap.fordeling.arkiv.journalpost.Journalpost
import no.nav.aap.fordeling.config.GlobalBeanConfig.Companion.clientCredentialFlow
import no.nav.aap.fordeling.egenansatt.EgenAnsattClient
import no.nav.aap.fordeling.navenhet.NAVEnhet
import no.nav.aap.fordeling.navenhet.NavEnhetClient
import no.nav.aap.fordeling.oppgave.OppgaveClient
import no.nav.aap.fordeling.person.Diskresjonskode
import no.nav.aap.fordeling.person.PDLClient
import no.nav.aap.util.Constants.AAP
import no.nav.aap.util.LoggerUtil.getLogger
import no.nav.aap.util.WebClientExtensions.response
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenService
import no.nav.security.token.support.client.spring.ClientConfigurationProperties
import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.security.token.support.spring.UnprotectedRestController

@UnprotectedRestController(value = ["/dev"])
class TestController(
    private val pdlClient : PDLClient,
    private val egenClient : EgenAnsattClient,
    private val arkivAdapter : DokarkivWebClientAdapter,
    private val arkivClient : ArkivClient,
    private val oppgaveClient : OppgaveClient,
    private val arenaAdapter : ArenaWebClientAdapter,
    private val pong : PongWebClientAdapter,
    private val orgClient : NavEnhetClient) {

    private val log = getLogger(javaClass)

    @ProtectedWithClaims(issuer = "aad")
    @GetMapping("test")
    fun test() = pong.pong()

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
    fun diskresjonskode(@RequestParam fnr : Fødselsnummer) = pdlClient.diskresjonskode(fnr)

    @GetMapping("gt")
    fun gt(@RequestParam fnr : Fødselsnummer) = pdlClient.geoTilknytning(fnr)

    @GetMapping("fnr")
    fun fnr(@RequestParam aktørId : AktørId) = pdlClient.fnr(aktørId)

    @PostMapping("opprettarenaoppgave")
    fun arenaOpprettOppgave(@RequestBody jp : Journalpost) = arenaAdapter.opprettArenaOppgave(jp, "666")
}

@Configuration(proxyBeanMethods = false)
class PongClientBeanConfig {

    @Bean
    @Qualifier("pong")
    fun pongWebClient(b : Builder, @Qualifier("pong") pongFlow : ExchangeFilterFunction) =
        b.baseUrl("http://demo.helseopplysninger")
            .filter(pongFlow)
            .build()

    @Bean
    @Qualifier("pong")
    fun pongFLow(cfg : ClientConfigurationProperties, service : OAuth2AccessTokenService) =
        cfg.clientCredentialFlow(service, "pong")
}

@Component
class PongWebClientAdapter(@Qualifier("pong") private val webClient : WebClient) {

    fun pong() = webClient
        .get()
        .uri(::path)
        .accept(APPLICATION_JSON)
        .exchangeToMono { it.response<String>() }
        .contextCapture()
        .block()

    fun path(b : UriBuilder) = b.path("/pong").build()
}