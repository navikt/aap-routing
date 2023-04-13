package no.nav.aap.fordeling.egenansatt

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpStatus.*
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import no.nav.aap.api.felles.error.IrrecoverableIntegrationException
import no.nav.aap.fordeling.egenansatt.EgenAnsattConfig.Companion.EGENANSATT
import no.nav.aap.rest.AbstractWebClientAdapter
import no.nav.aap.util.LoggerUtil
import no.nav.aap.util.WebClientExtensions.response

@Component
class EgenAnsattWebClientAdapter(@Qualifier(EGENANSATT) webClient : WebClient, val cf : EgenAnsattConfig) : AbstractWebClientAdapter(webClient, cf) {

    private val log = LoggerUtil.getLogger(EgenAnsattWebClientAdapter::class.java)

    fun erEgenAnsatt(fnr : String) =
        if (cf.isEnabled) {
            webClient.post()
                .uri(cf::skjermetUri)
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .bodyValue(fnr.toIdent())
                .exchangeToMono { it.response<Boolean>(log) }
                .retryWhen(cf.retrySpec(log, cf.path))
                .doOnSuccess { log.trace("Egen ansatt oppslag OK. Respons $it") }
                .doOnError { log.warn("Egen ansatt oppslag feilet", it) }
                .contextCapture()
                .block() ?: throw IrrecoverableIntegrationException("Null respons fra egen ansatt")
        }
        else {
            false.also {
                log.info("Slo IKKE opp egen ansatt, sett egenansatt.enabled=true for å aktivere")
            }
        }

    override fun toString() = "EgenAnsattWebClientAdapter(cf=$cf), ${super.toString()})"

    companion object {

        private fun String.toIdent() = Ident(this)
        private data class Ident(val personident : String)
    }
}