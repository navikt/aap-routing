package no.nav.aap.routing.egenansatt

import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.felles.error.IntegrationException
import no.nav.aap.rest.AbstractWebClientAdapter
import no.nav.aap.routing.egenansatt.EgenAnsattConfig.Companion.EGENANSATT
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpStatus.*
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

@Component
class EgenAnsattWebClientAdapter(@Qualifier(EGENANSATT) webClient: WebClient, val cf: EgenAnsattConfig) :
    AbstractWebClientAdapter(webClient, cf) {

        fun erSkjermet(id: String) = webClient.post()
            .uri { b -> b.path(cf.path).build() }
            .contentType(APPLICATION_JSON)
            .bodyValue(Ident(id))
            .retrieve()
            .bodyToMono<Boolean>()
            .retryWhen(cf.retrySpec(log))
            .doOnError { t: Throwable -> log.warn("Skjerming oppslag feilet", t) }
            .block() ?: throw IntegrationException("Null respons fra Skjerming")

    private data class Ident(val personident: String)

}

@Component
class EgenAnsattClient(private val a: EgenAnsattWebClientAdapter) {
    fun erSkjermet(fnr:Fødselsnummer) = a.erSkjermet(fnr.fnr)
}