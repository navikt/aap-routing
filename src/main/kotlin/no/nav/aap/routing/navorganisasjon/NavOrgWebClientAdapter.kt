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
            .block() ?: throw IntegrationException("Null respons fra best match NORG2")


    fun aktiveEnheter() = webClient.get()
        .uri { b -> b.path(cf.aktive)
            .queryParam(ENHETSLISTE, AKTIV)
            .build()}
        .accept(APPLICATION_JSON)
        .retrieve()
        .bodyToMono<List<NavOrg>>()
        .retryWhen(cf.retrySpec(log))
        .doOnError { t: Throwable -> log.warn("Aktive enheter oppslag feilet", t) }
        .block() ?: throw IntegrationException("Null respons fra aktive enheter NORG2")
}

@Component
class NavOrgClient(private val adapter: NavOrgWebClientAdapter) {

    @Cacheable(NAVORG)
    fun erAktiv(org: NavEnhet) = adapter.aktiveEnheter()
        .map { it.enhetNr }
        .filterNot {  it in listOf("1891", "1893") }
        .contains(org.enhetNr)
    fun navEnhet(område: String, skjermet: Boolean, diskresjonskode: Diskresjonskode) =
        adapter.bestMatch(EnhetsKriteria(område,skjermet,diskresjonskode)).first().tilNavEnhet()
}

//return aktiveNavEnheter.contains(enhetId) && !UNTAKSENHETER.contains(enhetId);