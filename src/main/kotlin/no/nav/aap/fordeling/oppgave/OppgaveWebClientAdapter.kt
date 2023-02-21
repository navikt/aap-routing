package no.nav.aap.fordeling.oppgave

import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.felles.error.IntegrationException
import no.nav.aap.fordeling.egenansatt.EgenAnsattConfig.Companion.EGENANSATT
import no.nav.aap.fordeling.egenansatt.EgenAnsattWebClientAdapter
import no.nav.aap.fordeling.oppgave.OppgaveConfig.Companion.OPPGAVE
import no.nav.aap.rest.AbstractWebClientAdapter
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpStatus.*
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

@Component
class OppgaveWebClientAdapter(@Qualifier(OPPGAVE) webClient: WebClient, val cf: OppgaveConfig) :
    AbstractWebClientAdapter(webClient, cf) {

        fun opprettOppgave(id: String) = Unit /* webClient.post()
            .uri { b -> b.path(cf.path).build() }
            .contentType(APPLICATION_JSON)
            .bodyValue(Ident(id))
            .retrieve()
            .bodyToMono<Boolean>()
            .retryWhen(cf.retrySpec(log))
            .doOnError { t: Throwable -> log.warn("Skjerming oppslag feilet", t) }
            .block() ?: throw IntegrationException("Null respons fra Skjerming")*/
}

@Component
class OppgaveClient(private val a: OppgaveWebClientAdapter)