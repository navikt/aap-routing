package no.nav.aap.fordeling.egenansatt

import no.nav.aap.api.felles.error.IrrecoverableIntegrationException
import no.nav.aap.fordeling.egenansatt.EgenAnsattConfig.Companion.EGENANSATT
import no.nav.aap.rest.AbstractWebClientAdapter
import no.nav.aap.util.WebClientExtensions.toResponse
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpStatus.*
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient

@Component
class EgenAnsattWebClientAdapter(@Qualifier(EGENANSATT) webClient : WebClient, val cf : EgenAnsattConfig) :
    AbstractWebClientAdapter(webClient, cf) {

    fun erEgenAnsatt(fnr : String) = webClient.post()
        .uri(cf::skjermetUri)
        .contentType(APPLICATION_JSON)
        .accept(APPLICATION_JSON)
        .bodyValue(fnr.toIdent())
        .exchangeToMono { it.toResponse<Boolean>(log) }
        .retryWhen(cf.retrySpec(log, cf.path))
        .doOnSuccess { log.trace("Egen ansatt oppslag OK. Respons $it") }
        .doOnError { t -> log.warn("Egen ansatt oppslag feilet", t) }
        .block() ?: throw IrrecoverableIntegrationException("Null respons fra egen ansatt")

    override fun toString() = "EgenAnsattWebClientAdapter(cf=$cf), ${super.toString()})"

    companion object {
        private fun String.toIdent() = Ident(this)
        private data class Ident(val personident : String)
    }
}