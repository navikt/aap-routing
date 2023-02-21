package no.nav.aap.fordeling.arena

import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.felles.SkjemaType.*
import no.nav.aap.api.felles.error.IntegrationException
import no.nav.aap.fordeling.arena.ArenaConfig.Companion.ARENA
import no.nav.aap.fordeling.arena.ArenaDTOs.*
import no.nav.aap.fordeling.arkiv.Journalpost
import no.nav.aap.fordeling.navorganisasjon.NavEnhet
import no.nav.aap.rest.AbstractWebClientAdapter
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
            .retrieve()
            .bodyToMono<String>()
            .retryWhen(cf.retrySpec(log))
            .doOnSuccess { log.info("Arena nyeste aktive sak for $fnr er $it") }
            .doOnError { t -> log.warn("Arena nyeste aktive sak oppslag feilet", t) }
            .block()

    fun opprettArenaOppgave(journalpost: Journalpost, enhet: NavEnhet) =
        with(journalpost) {
            webClient.post()
                .uri { b -> b.path(cf.oppgavePath).build() }
                .contentType(APPLICATION_JSON)
                .bodyValue(ArenaOpprettOppgave(fnr,enhet.enhetNr,dokumenter.first().tittel ?: STANDARD.tittel,
                        dokumenter.drop(1).mapNotNull { it.tittel }))
                .retrieve()
                .bodyToMono<ArenaOpprettetOppgave>()
                .doOnSuccess { log.info("Arena opprettet oppgave er $it") }
                .doOnError { t -> log.warn("Arena opprett oppgave feilet", t) }
                .block() ?: throw IntegrationException("Null respons for opprettelse av oppgave")
        }
}