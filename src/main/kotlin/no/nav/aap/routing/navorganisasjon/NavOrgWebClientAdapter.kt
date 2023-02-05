package no.nav.aap.routing.navorganisasjon

import no.nav.aap.api.felles.error.IntegrationException
import no.nav.aap.rest.AbstractWebClientAdapter
import no.nav.aap.routing.navorganisasjon.NavOrgConfig.Companion.AKTIV
import no.nav.aap.routing.navorganisasjon.NavOrgConfig.Companion.ENHETSLISTE
import no.nav.aap.routing.navorganisasjon.NavOrgConfig.Companion.NAVORG
import no.nav.aap.routing.person.Diskresjonskode
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

        fun bestMatch(kriteria: EnhetsKriteria) = webClient.post()
            .uri { b -> b.path(cf.enhet).build() }
            .contentType(APPLICATION_JSON)
            .bodyValue(kriteria)
            .retrieve()
            .bodyToMono<List<NavOrg>>()
            .retryWhen(cf.retrySpec(log))
            .doOnError { t: Throwable -> log.warn("best match oppslag feilet", t) }
            .block()
            ?.firstOrNull { aktiveEnheter().find { it.enhetNr == it.enhetNr}  != null }
            ?.tilNavEnhet()
            ?: throw IntegrationException("Ingen best match Nav enhet fra NORG2")


    @Cacheable(NAVORG)
    fun aktiveEnheter() = webClient.get()
        .uri { b -> b.path(cf.aktive).queryParam(ENHETSLISTE, AKTIV).build()}
        .accept(APPLICATION_JSON)
        .retrieve()
        .bodyToMono<List<NavOrg>>()
        .retryWhen(cf.retrySpec(log))
        .doOnError { t: Throwable -> log.warn("Aktive enheter oppslag feilet", t) }
        .block()
        ?.filterNot {  it.enhetNr in UNTATTE_ENHETER }
        ?: throw IntegrationException("Null respons fra aktive enheter NORG2")

    companion object {
        private val UNTATTE_ENHETER = listOf("1891", "1893")
    }

}

@Component
class NavOrgClient(private val adapter: NavOrgWebClientAdapter) {
    fun navEnhet(område: String, skjermet: Boolean, diskresjonskode: Diskresjonskode) =
        adapter.bestMatch(EnhetsKriteria(område,skjermet,diskresjonskode))
}