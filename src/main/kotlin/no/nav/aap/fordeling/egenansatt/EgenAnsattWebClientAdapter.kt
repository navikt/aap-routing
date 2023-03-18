package no.nav.aap.fordeling.egenansatt

import no.nav.aap.api.felles.error.IntegrationException
import no.nav.aap.fordeling.egenansatt.EgenAnsattConfig.Companion.EGENANSATT
import no.nav.aap.rest.AbstractWebClientAdapter
import no.nav.aap.util.Metrics
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpStatus.*
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

@Component
class EgenAnsattWebClientAdapter(@Qualifier(EGENANSATT) webClient: WebClient, val cf: EgenAnsattConfig,metrikker: Metrics) :
    AbstractWebClientAdapter(webClient, cf, metrikker = metrikker) {

    fun erSkjermet(fnr: String) = webClient.post()
        .uri(cf::skjermetUri)
        .contentType(APPLICATION_JSON)
        .accept(APPLICATION_JSON)
        .bodyValue(Ident(fnr))
        .retrieve()
        .bodyToMono<Boolean>()
        .retryWhen(cf.retrySpec(log, metrikker = metrikker))
        .doOnSuccess { log.info("Skjerming oppslag OK. Respons $it") }
        .doOnError { t -> log.warn("Skjerming oppslag feilet", t) }
        .block() ?: throw IntegrationException("Null respons fra Skjerming")

    private data class Ident(val personident: String)
}