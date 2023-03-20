package no.nav.aap.fordeling.arena

import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.felles.SkjemaType.*
import no.nav.aap.api.felles.error.IrrecoverableIntegrationException
import no.nav.aap.fordeling.arena.ArenaConfig.Companion.ARENA
import no.nav.aap.fordeling.arena.ArenaDTOs.ArenaOpprettOppgaveData
import no.nav.aap.fordeling.arena.ArenaDTOs.ArenaOpprettetOppgave
import no.nav.aap.fordeling.arena.ArenaDTOs.ArenaOpprettetOppgave.Companion.EMPTY
import no.nav.aap.fordeling.util.WebClientExtensions.toResponse
import no.nav.aap.rest.AbstractWebClientAdapter
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpStatus.*
import org.springframework.http.MediaType.*
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient

@Component
class ArenaWebClientAdapter(@Qualifier(ARENA) webClient: WebClient, val cf: ArenaConfig) :
    AbstractWebClientAdapter(webClient, cf) {

    fun nyesteArenaSak(fnr: Fødselsnummer) =
        webClient.get()
            .uri { cf.nyesteSakUri(it, fnr) }
            .accept(APPLICATION_JSON)
            .exchangeToMono { it.toResponse<String>(log)}
            .retryWhen(cf.retrySpec(log,cf.nyesteSakPath))
            .doOnSuccess { log.info("Arena oppslag nyeste oppgavce OK. Respons $it") }
            .doOnError { t -> log.warn("Arena nyeste aktive sak oppslag feilet", t) }
            .block()

    fun opprettArenaOppgave(data: ArenaOpprettOppgaveData) =
        if (cf.isEnabled) {
            webClient.post()
                .uri(cf::oppgaveUri)
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .bodyValue(data)
                .exchangeToMono { it.toResponse<ArenaOpprettetOppgave>(log)}
                .retryWhen(cf.retrySpec(log,cf.oppgavePath))
                .doOnSuccess { log.info("Arena opprettet oppgave OK. Respons $it") }
                .doOnError { t -> log.warn("Arena opprett oppgave feilet (${t.message})", t) }
                .block() ?: throw IrrecoverableIntegrationException("Null respons for opprettelse av oppgave")
        }
        else {
            EMPTY.also {
                log.info("Opprettet ikke arena oppgave med data fra $data")
            }
        }
}