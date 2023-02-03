package no.nav.aap.routing.navorganisasjon

import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.felles.error.IntegrationException
import no.nav.aap.rest.AbstractWebClientAdapter
import no.nav.aap.routing.navorganisasjon.NavOrgConfig.Companion.ORG
import no.nav.aap.routing.person.Diskresjonskode
import no.nav.aap.routing.person.PDLWebClientAdapter
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpStatus.*
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

@Component
class NavOrgWebClientAdapter(@Qualifier(ORG) webClient: WebClient, val cf: NavOrgConfig) :
    AbstractWebClientAdapter(webClient, cf) {

        fun bestMatch(kriteria: EnhetsKriteria) = webClient.post()
            .uri { b -> b.path(cf.bestMatch).build() }
            .contentType(APPLICATION_JSON)
            .bodyValue(kriteria)
            .retrieve()
            .bodyToMono<Map<String, Any>>()
            .retryWhen(cf.retrySpec(log))
            .doOnError { t: Throwable -> log.warn("BestMatch oppslag feilet", t) }
            .block() ?: throw IntegrationException("Null respons fra NORG2")

}

@Component
class NavOrgClient(private val adapter: NavOrgWebClientAdapter) {
    fun bestMatch(område: String, skjermet: Boolean,diskresjonskode: Diskresjonskode) = adapter.bestMatch(EnhetsKriteria(område,skjermet,diskresjonskode))
}