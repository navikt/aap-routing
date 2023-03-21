package no.nav.aap.fordeling.navenhet

import no.nav.aap.api.felles.error.IrrecoverableIntegrationException
import no.nav.aap.fordeling.navenhet.EnhetsKriteria.NavOrg
import no.nav.aap.fordeling.navenhet.EnhetsKriteria.NavOrg.NAVEnhet
import no.nav.aap.fordeling.navenhet.NavEnhetConfig.Companion.NAVENHET
import no.nav.aap.fordeling.util.WebClientExtensions.toResponse
import no.nav.aap.rest.AbstractWebClientAdapter
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpStatus.*
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient

@Component
class NavEnhetWebClientAdapter(@Qualifier(NAVENHET) webClient: WebClient, val cf: NavEnhetConfig) :
    AbstractWebClientAdapter(webClient, cf) {

    fun navEnhet(kriterium: EnhetsKriteria, enheter: List<NavOrg>) = webClient.post()
        .uri(cf::enhetUri)
        .contentType(APPLICATION_JSON)
        .accept(APPLICATION_JSON)
        .bodyValue(kriterium)
        .exchangeToMono { it.toResponse<List<NavOrg>>(log)}
        .retryWhen(cf.retrySpec(log,cf.enhet))
        .doOnSuccess { log.info("Nav enhet oppslag med $kriterium mot NORG2 OK. Respons $it") }
        .doOnError { t -> log.warn("Nav enhet oppslag med $kriterium mot NORG2 feilet", t) }
        .block()
        ?.filterNot(::untatt)
        ?.firstOrNull { it in enheter }
        ?.let { NAVEnhet(it.enhetNr) }
        ?: throw IrrecoverableIntegrationException("Ingen Nav enhet for $kriterium fra NORG2")

    fun aktiveEnheter() = webClient.get()
        .uri(cf::aktiveEnheterUri)
        .accept(APPLICATION_JSON)
        .exchangeToMono { it.toResponse<List<NavOrg>>(log)}
        .retryWhen(cf.retrySpec(log,cf.aktive))
        .doOnSuccess { log.info("Aktive enheter oppslag  NORG2 OK. Respons ga ${it.size} innslag") }
        .doOnError { t -> log.warn("Aktive enheter oppslag feilet", t) }
        .block() ?: throw IrrecoverableIntegrationException("Kunne ikke hente aktive enheter")

    companion object {
        private val UNTATTE_ENHETER = listOf("1891", "1893")
        private fun untatt(org: NavOrg) = org.enhetNr in UNTATTE_ENHETER
    }
}