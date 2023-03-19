package no.nav.aap.fordeling.egenansatt

import java.io.IOException
import java.time.Duration
import java.util.function.Predicate
import no.nav.aap.api.felles.error.IntegrationException
import no.nav.aap.fordeling.egenansatt.EgenAnsattConfig.Companion.EGENANSATT
import no.nav.aap.rest.AbstractWebClientAdapter
import org.apache.commons.lang3.exception.ExceptionUtils.hasCause
import org.slf4j.Logger
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpStatus.*
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.WebClientResponseException.BadRequest
import org.springframework.web.reactive.function.client.WebClientResponseException.Forbidden
import org.springframework.web.reactive.function.client.WebClientResponseException.NotFound
import org.springframework.web.reactive.function.client.WebClientResponseException.Unauthorized
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.util.retry.Retry.RetrySignal
import reactor.util.retry.Retry.fixedDelay

@Component
class EgenAnsattWebClientAdapter(@Qualifier(EGENANSATT) webClient: WebClient, val cf: EgenAnsattConfig) :
    AbstractWebClientAdapter(webClient, cf) {

    fun erSkjermet(fnr: String) = webClient.post()
        .uri(cf::skjermetUri)
        .contentType(APPLICATION_JSON)
        .accept(APPLICATION_JSON)
        .bodyValue(Ident(fnr))
        .retrieve()
        .bodyToMono<Boolean>()
        .retryWhen(cf.retrySpec(log, cf.path))
        .doOnSuccess { log.info("Skjerming oppslag OK. Respons $it") }
        .onErrorMap { throw IntegrationException(it.message,cf.baseUri, it) }
      //  .doOnError { t -> log.warn("Skjerming oppslag feilet", t) }
        .block() ?: throw IntegrationException("Null respons fra skjerming")

    private data class Ident(val personident: String)
}