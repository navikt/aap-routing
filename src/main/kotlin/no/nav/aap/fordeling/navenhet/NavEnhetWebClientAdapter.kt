package no.nav.aap.fordeling.navenhet

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import no.nav.aap.api.felles.error.IrrecoverableIntegrationException
import no.nav.aap.fordeling.navenhet.NavEnhetConfig.Companion.NAVENHET
import no.nav.aap.rest.AbstractWebClientAdapter
import no.nav.aap.util.LoggerUtil
import no.nav.aap.util.WebClientExtensions.response

@Component
class NavEnhetWebClientAdapter(@Qualifier(NAVENHET) webClient : WebClient, val cf : NavEnhetConfig) : AbstractWebClientAdapter(webClient, cf) {

    private val log = LoggerUtil.getLogger(NavEnhetWebClientAdapter::class.java)

    fun navEnhet(kriterium : EnhetsKriteria, enheter : List<NAVEnhet>) = webClient
        .post()
        .uri(cf::enhetUri)
        .contentType(APPLICATION_JSON)
        .accept(APPLICATION_JSON)
        .bodyValue(kriterium)
        .exchangeToMono { it.response<List<Map<String, String>>>(log) }
        .retryWhen(cf.retrySpec(log, cf.enhet))
        .doOnSuccess { log.info("Nav enhet oppslag mot NORG2 OK for kriteria $kriterium.") }
        .doOnError { t -> log.warn("Nav enhet oppslag med $kriterium mot NORG2 feilet", t) }
        .contextCapture()
        .block()
        ?.map { NAVEnhet(it["enhetNr"]!!) }
        ?.firstOrNull { it in enheter }

    fun aktiveEnheter() = webClient.get()
        .uri(cf::aktiveEnheterUri)
        .accept(APPLICATION_JSON)
        .exchangeToMono { it.response<List<Map<String, Any>>>(log) }
        .retryWhen(cf.retrySpec(log, cf.aktive))
        .doOnSuccess { log.trace("Aktive enheter oppslag  NORG2 OK. Respons med ${it.size} innslag") }
        .doOnError { t -> log.warn("Aktive enheter oppslag feilet", t) }
        .block()?.map { NAVEnhet("${it["enhetNr"]}") }
        ?.filterNot(NAVEnhet::untatt)
        ?: throw IrrecoverableIntegrationException("Kunne ikke hente aktive enheter")

    override fun toString() = "NavEnhetWebClientAdapter(cf=cf, ${super.toString()})"
}