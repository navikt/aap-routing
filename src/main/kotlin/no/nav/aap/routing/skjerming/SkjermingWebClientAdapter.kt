package no.nav.aap.routing.skjerming

import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.felles.error.IntegrationException
import no.nav.aap.rest.AbstractWebClientAdapter
import no.nav.aap.routing.navorganisasjon.EnhetsKriteria
import no.nav.aap.routing.navorganisasjon.NavOrgConfig.Companion.ORG
import no.nav.aap.routing.navorganisasjon.NavOrgWebClientAdapter
import no.nav.aap.routing.person.PDLWebClientAdapter
import no.nav.aap.routing.skjerming.SkjermingConfig.Companion.SKJERMING
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpStatus.*
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

@Component
class SkjermingWebClientAdapter(@Qualifier(SKJERMING) webClient: WebClient, val cf: SkjermingConfig) :
    AbstractWebClientAdapter(webClient, cf) {

        fun erSkjermet(fnr: Fødselsnummer) = webClient.post()
            .uri { b -> b.path(cf.path).build() }
            .contentType(APPLICATION_JSON)
            .bodyValue(fnr.fnr)
            .retrieve()
            .bodyToMono<String>()
            .retryWhen(cf.retrySpec(log))
            .doOnError { t: Throwable -> log.warn("Skjerming oppslag feilet", t) }
            .block() ?: throw IntegrationException("Null respons fra Skjerming")

}

@Component
class SkjermingClient(private val adapter: SkjermingWebClientAdapter) {
    fun erSkjermet(fnr: Fødselsnummer) = adapter.erSkjermet(fnr)
}