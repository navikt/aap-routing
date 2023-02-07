package no.nav.aap.fordeling.arena

import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.felles.error.IntegrationException
import no.nav.aap.rest.AbstractWebClientAdapter
import no.nav.aap.fordeling.arena.ArenaConfig.Companion.ARENA
import no.nav.aap.fordeling.arena.ArenaConfig.Companion.ENHET
import no.nav.aap.fordeling.arena.ArenaDTOs.ArenaSakForespørsel
import no.nav.aap.fordeling.arkiv.Journalpost
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpStatus.*
import org.springframework.http.MediaType.*
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

@Component
class ArenaWebClientAdapter(@Qualifier(ARENA) webClient: WebClient, val cf: ArenaConfig) :
    AbstractWebClientAdapter(webClient, cf) {

    fun hentSaker(fnr: Fødselsnummer, enhetNr: String) =
        webClient.post()
            .uri { b -> b.path(cf.sakerPath).build() }
            .contentType(APPLICATION_JSON)
            .bodyValue(ArenaSakForespørsel(fnr))
            .header(ENHET,enhetNr)
            .retrieve()
            .bodyToMono<List<Map<String,Any>>>()
            .retryWhen(cf.retrySpec(log))
            .doOnError { t: Throwable -> log.warn("Arena sak oppslag feilet", t) }
            .block() ?: throw IntegrationException("Null respons fra arena sak")
}

@Component
class ArenaClient(private val adapter: ArenaWebClientAdapter) {
    fun harArenaSak(journalpost: Journalpost, enhetNr: String) = adapter.hentSaker(journalpost.fnr,enhetNr) != null // TODO
}