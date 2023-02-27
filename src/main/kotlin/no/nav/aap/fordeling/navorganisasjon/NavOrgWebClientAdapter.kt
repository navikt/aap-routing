package no.nav.aap.fordeling.navorganisasjon

import no.nav.aap.api.felles.error.IntegrationException
import no.nav.aap.fordeling.arkiv.FordelingException
import no.nav.aap.fordeling.navorganisasjon.NavOrgConfig.Companion.AKTIV
import no.nav.aap.fordeling.navorganisasjon.NavOrgConfig.Companion.ENHETSLISTE
import no.nav.aap.fordeling.navorganisasjon.NavOrgConfig.Companion.NAVORG
import no.nav.aap.rest.AbstractWebClientAdapter
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.cache.annotation.Cacheable
import org.springframework.http.HttpStatus.*
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

@Component
class NavOrgWebClientAdapter(@Qualifier(NAVORG) webClient: WebClient, val cf: NavOrgConfig) :
    AbstractWebClientAdapter(webClient, cf) {

        fun navEnhet(kriterium: EnhetsKriteria, enheter: List<NavOrg>) = webClient.post()
            .uri { b -> b.path(cf.enhet).build() }
            .contentType(APPLICATION_JSON)
            .bodyValue(kriterium)
            .retrieve()
            .bodyToMono<List<NavOrg>>()
            .retryWhen(cf.retrySpec(log))
            .doOnError { t -> log.warn("Nav enhet oppslag med $kriterium mot NORG2 feilet", t) }
            .doOnSuccess {log.info("Nav enhet oppslag med $kriterium mot NORG2 OK er $it") }
            .block()
            ?.filterNot(::untatt)
            ?.firstOrNull { it in enheter }
            ?.tilNavEnhet()
            ?: throw FordelingException("Ingen Nav enhet for $kriterium fra NORG2")


    @Cacheable(NAVORG)
    fun aktiveEnheter() = webClient.get()
        .uri { b -> b.path(cf.aktive).queryParam(ENHETSLISTE, AKTIV).build() }
        .accept(APPLICATION_JSON)
        .retrieve()
        .bodyToMono<List<NavOrg>>()
        .retryWhen(cf.retrySpec(log))
        .doOnSuccess {log.info("Aktive enheter oppslag  NORG2 OK fant  ${it.size} innsøag") }
        .doOnError { t -> log.warn("Aktive enheter oppslag feilet", t) }
        .block()
        ?: throw IntegrationException("Kunne ikke hente aktive enheter")

    companion object {
        private val UNTATTE_ENHETER = listOf("1891", "1893")
        private fun untatt(org: NavOrg) = org.enhetNr in UNTATTE_ENHETER
    }
}