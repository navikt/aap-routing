package no.nav.aap.fordeling.arena

import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.felles.SkjemaType.*
import no.nav.aap.api.felles.error.IntegrationException
import no.nav.aap.fordeling.arena.ArenaConfig.Companion.ARENA
import no.nav.aap.fordeling.arena.ArenaDTOs.*
import no.nav.aap.fordeling.arena.ArenaDTOs.ArenaOpprettetOppgave.Companion.EMPTY
import no.nav.aap.rest.AbstractWebClientAdapter
import org.checkerframework.checker.units.qual.t
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpStatus.*
import org.springframework.http.MediaType.*
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

@Component
class ArenaWebClientAdapter(@Qualifier(ARENA) webClient: WebClient, val cf: ArenaConfig) :
    AbstractWebClientAdapter(webClient, cf) {

    fun nyesteArenaSak(fnr: Fødselsnummer) =
        webClient.get()
            .uri { b -> b.path(cf.nyesteSakPath).build(fnr.fnr) }
            .accept(APPLICATION_JSON)
            .retrieve()
            .bodyToMono<String>()
            .retryWhen(cf.retrySpec(log))
            .doOnSuccess { log.info("Arena oppslag nyeste oppgavce OK. Respons $it") }
            .doOnError { t -> log.warn("Arena nyeste aktive sak oppslag feilet", t) }
            .block()

    fun opprettArenaOppgave(data: ArenaOpprettOppgaveData) =
        if (cf.isEnabled) {
            webClient.post()
                .uri { b -> b.path(cf.oppgavePath).build() }
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .bodyValue(data)
                .retrieve()
                .bodyToMono<ArenaOpprettetOppgave>()
                .retryWhen(cf.retrySpec(log))
                .doOnSuccess { log.info("Arena opprettet oppgave OK. Respons $it") }
                .doOnError { t -> log.warn("Arena opprett oppgave feilet", t) }
                .block() ?: throw IntegrationException("Null respons for opprettelse av oppgave")
        }
        else {
            EMPTY.also {
                log.info("Opprettet ikke arena oppgave med data $data")
            }
        }


}