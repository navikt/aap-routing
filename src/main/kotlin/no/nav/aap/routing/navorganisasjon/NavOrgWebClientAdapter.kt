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

        fun navEnhet(kriterium: EnhetsKriteria) = webClient.post()
            .uri { b -> b.path(cf.enhet).build() }
            .contentType(APPLICATION_JSON)
            .bodyValue(kriterium)
            .retrieve()
            .bodyToMono<List<NavOrg>>()
            .retryWhen(cf.retrySpec(log))
            .doOnError { t -> log.warn("Nav enhet oppslag NORG2 feilet", t) }
            .block()
            ?.firstOrNull(::erAktiv)
            ?.tilNavEnhet()
            ?: throw IntegrationException("Ingen Nav enhet for $kriterium fra NORG2")


    @Cacheable(NAVORG)
    fun erAktiv(org: NavOrg) = webClient.get()
        .uri { b -> b.path(cf.aktive).queryParam(ENHETSLISTE, AKTIV).build() }
        .accept(APPLICATION_JSON)
        .retrieve()
        .bodyToMono<List<NavOrg>>()
        .retryWhen(cf.retrySpec(log))
        .doOnError { t -> log.warn("Aktive enheter oppslag feilet", t) }
        .block()
        ?.filterNot { it.enhetNr in UNTATTE_ENHETER }
        ?.any { it.enhetNr == org.enhetNr }
        ?: throw IntegrationException("Null respons fra aktive enheter NORG2")

    companion object {
        private val UNTATTE_ENHETER = listOf("1891", "1893")
    }

}

@Component
class NavOrgClient(private val adapter: NavOrgWebClientAdapter) {
    fun navEnhet(område: String, skjermet: Boolean, diskresjonskode: Diskresjonskode) =
        adapter.navEnhet(EnhetsKriteria(område,skjermet,diskresjonskode))
}