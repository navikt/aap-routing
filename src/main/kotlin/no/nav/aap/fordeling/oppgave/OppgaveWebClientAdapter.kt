package no.nav.aap.fordeling.oppgave

import no.nav.aap.api.felles.error.IntegrationException
import no.nav.aap.fordeling.oppgave.OppgaveConfig.Companion.OPPGAVE
import no.nav.aap.rest.AbstractWebClientAdapter
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpStatus.*
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

@Component
class OppgaveWebClientAdapter(@Qualifier(OPPGAVE) webClient: WebClient, val cf: OppgaveConfig) :
    AbstractWebClientAdapter(webClient, cf) {

    fun harOppgave(journalpostId: String) =  webClient.get()
        .uri{b -> cf.oppgaveUri(b,journalpostId)}
        .retrieve()
        .bodyToMono<Boolean>()
        .retryWhen(cf.retrySpec(log))
        .doOnSuccess { log.info("Oppgave oppslag $it") }
        .doOnError { t: Throwable -> log.warn("Oppgave oppslag feilet", t) }
        .block() ?: throw IntegrationException("Null respons fra oppslag oppgave")
}

@Component
class OppgaveClient(private val a: OppgaveWebClientAdapter) {
    fun harOppgave(journalpostId: String) = a.harOppgave(journalpostId)

}