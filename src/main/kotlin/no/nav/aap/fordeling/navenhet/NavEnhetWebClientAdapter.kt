package no.nav.aap.fordeling.navenhet

import no.nav.aap.api.felles.error.IrrecoverableIntegrationException
import no.nav.aap.fordeling.navenhet.EnhetsKriteria.NavOrg
import no.nav.aap.fordeling.navenhet.EnhetsKriteria.NavOrg.Companion.untatt
import no.nav.aap.fordeling.navenhet.EnhetsKriteria.NavOrg.NAVEnhet
import no.nav.aap.fordeling.navenhet.NavEnhetConfig.Companion.NAVENHET
import no.nav.aap.rest.AbstractWebClientAdapter
import no.nav.aap.util.LoggerUtil
import no.nav.aap.util.WebClientExtensions.toResponse
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient

@Component
class NavEnhetWebClientAdapter(@Qualifier(NAVENHET) webClient : WebClient, val cf : NavEnhetConfig) : AbstractWebClientAdapter(webClient, cf) {

    private val log = LoggerUtil.getLogger(NavEnhetWebClientAdapter::class.java)

    fun navEnhet(kriterium : EnhetsKriteria, enheter : List<NavOrg>) = webClient
        .post()
        .uri(cf::enhetUri)
        .contentType(APPLICATION_JSON)
        .accept(APPLICATION_JSON)
        .bodyValue(kriterium)
        .exchangeToMono { it.toResponse<List<Map<String, String>>>(log) }
        .retryWhen(cf.retrySpec(log, cf.enhet))
        .doOnSuccess { log.trace("Nav enhet oppslag mot NORG2 OK.") }
        .doOnError { t -> log.warn("Nav enhet oppslag med $kriterium mot NORG2 feilet", t) }
        .block()
        ?.map { NavOrg(it["enhetNr"]!!, it["status"]!!) }
        ?.filterNot(::untatt)
        ?.firstOrNull { it in enheter }
        ?.let { NAVEnhet(it.enhetNr) }

    fun aktiveEnheter() = webClient.get()
        .uri(cf::aktiveEnheterUri)
        .accept(APPLICATION_JSON)
        .exchangeToMono { it.toResponse<List<Map<String, Any>>>(log) }
        .retryWhen(cf.retrySpec(log, cf.aktive))
        .doOnSuccess { log.trace("Aktive enheter oppslag  NORG2 OK. Respons med ${it.size} innslag") }
        .doOnError { t -> log.warn("Aktive enheter oppslag feilet", t) }
        .block()?.map { NavOrg("${it["enhetNr"]}", "${it["status"]}") }
        ?: throw IrrecoverableIntegrationException("Kunne ikke hente aktive enheter")

    override fun toString() = "NavEnhetWebClientAdapter(cf=cf, ${super.toString()})"
}