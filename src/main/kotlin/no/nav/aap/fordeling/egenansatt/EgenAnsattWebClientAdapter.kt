package no.nav.aap.fordeling.egenansatt

import no.nav.aap.api.felles.error.IntegrationException
import no.nav.aap.fordeling.egenansatt.EgenAnsattConfig.Companion.EGENANSATT
import no.nav.aap.rest.AbstractWebClientAdapter
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpStatus.*
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

@Component
class EgenAnsattWebClientAdapter(@Qualifier(EGENANSATT) webClient: WebClient, val cf: EgenAnsattConfig) :
    AbstractWebClientAdapter(webClient, cf) {

        fun erSkjermet(fnr: String) = webClient.post()
            .uri { b -> b.path(cf.path).build() }
            .contentType(APPLICATION_JSON)
            .bodyValue(Ident(fnr))
            .retrieve()
            .bodyToMono<Boolean>()
            .retryWhen(cf.retrySpec(log))
            .doOnSuccess { log.info("Skjerming oppslag $it") }
            .doOnError { t: Throwable -> log.warn("Skjerming oppslag feilet", t) }
            .block() ?: throw IntegrationException("Null respons fra Skjerming")

    private data class Ident(val personident: String)
}