package no.nav.aap.fordeling.oppgave

import no.nav.aap.api.felles.error.IntegrationException
import no.nav.aap.fordeling.oppgave.OppgaveConfig.Companion.OPPGAVE
import no.nav.aap.fordeling.oppgave.OppgaveDTOs.OppgaveRespons
import no.nav.aap.rest.AbstractWebClientAdapter
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

@Component
class OppgaveWebClientAdapter(@Qualifier(OPPGAVE) webClient: WebClient, val cf: OppgaveConfig) :
    AbstractWebClientAdapter(webClient, cf) {

    fun harOppgave(journalpostId: String) =  webClient.get()
        .uri{b -> cf.oppgaveUri(b,journalpostId)}
        .retrieve()
        .bodyToMono<OppgaveRespons>()
        .retryWhen(cf.retrySpec(log))
        .doOnSuccess { log.info("Oppgave oppslag $it") }
        .doOnError { t -> log.warn("Oppgave oppslag feilet", t) }
        .block()?.antallTreffTotalt?.let { it > 0 } ?: throw IntegrationException("Null respons fra opslag oppgave")

    fun opprettOppgave(data: OpprettOppgaveData)  =
        webClient.post()
            .uri{b -> b.path(cf.oppgavePath).build()}
            .contentType(APPLICATION_JSON)
            .bodyValue(data)
            .retrieve()
            .bodyToMono<Any>()// TODO?
            .retryWhen(cf.retrySpec(log))
            .doOnSuccess { log.info("Opprett oppgave $it") }
            .doOnError { t -> log.warn("Opprett opprett  feilet", t) }
            .block() ?: throw IntegrationException("Null respons fra opprett oppgave")

}