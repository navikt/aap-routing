package no.nav.aap.fordeling.egenansatt

import no.nav.aap.api.felles.error.IrrecoverableIntegrationException
import no.nav.aap.fordeling.egenansatt.EgenAnsattConfig.Companion.EGENANSATT
import no.nav.aap.fordeling.util.WebClientExtensions.toResponse
import no.nav.aap.rest.AbstractWebClientAdapter
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpStatus.*
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
@Component
class EgenAnsattWebClientAdapter(@Qualifier(EGENANSATT) webClient: WebClient, val cf: EgenAnsattConfig) :
    AbstractWebClientAdapter(webClient, cf) {

    fun erEgennsatt(fnr: String) = webClient.post()
        .uri(cf::skjermetUri)
        .contentType(APPLICATION_JSON)
        .accept(APPLICATION_JSON)
        .bodyValue(Ident(fnr))
        .exchangeToMono { it.toResponse<Boolean>(log)}
        .retryWhen(cf.retrySpec(log, cf.path))
        .doOnSuccess { log.info("Skjerming oppslag OK. Respons $it") }
        .doOnError { t -> log.warn("Skjerming oppslag feilet", t) }
        .block() ?: throw IrrecoverableIntegrationException("Null respons fra skjerming")

    private data class Ident(val personident: String)

}