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

    fun harOppgave(journalpostId: String) =
        webClient.get()
            .uri { cf.oppgaveUri(it, journalpostId) }
            .retrieve()
            .bodyToMono<OppgaveRespons>()
            .retryWhen(cf.retrySpec(log))
            .doOnSuccess { log.info("Oppgave oppslag journalpost  $journalpostId OK. Respons $it") }
            .doOnError { t -> log.warn("Oppgave oppslag journalpost  $journalpostId feilet (${t.message})", t) }
            .block()?.antallTreffTotalt?.let { it > 0 }
            ?: throw IntegrationException("Null respons fra opslag oppgave $journalpostId")

    fun opprettOppgave(data: OpprettOppgaveData) =
        if (cf.isEnabled) {
            webClient.post()
                .uri(cf::opprettOppgaveUri)
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .bodyValue(data)
                .retrieve()
                .bodyToMono<Any>()// TODO?
                .retryWhen(cf.retrySpec(log))
                .doOnSuccess { log.info("Opprett oppgave fra $data OK. Respons $it") }
                .doOnError { t -> log.warn("Opprett oppgave fra $data  feilet", t) }
                .block() ?: throw IntegrationException("Null respons fra opprett oppgave fr $data")
        }
        else {
            log.info("Oppretter ikke oppgave for data $data")
        }
}