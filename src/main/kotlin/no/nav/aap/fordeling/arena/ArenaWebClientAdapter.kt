package no.nav.aap.fordeling.arena

import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.felles.SkjemaType.*
import no.nav.aap.api.felles.error.IrrecoverableIntegrationException
import no.nav.aap.fordeling.arena.ArenaConfig.Companion.ARENA
import no.nav.aap.fordeling.arena.ArenaDTOs.ArenaOpprettOppgaveData
import no.nav.aap.fordeling.arena.ArenaDTOs.ArenaOpprettetOppgave
import no.nav.aap.fordeling.arena.ArenaDTOs.ArenaOpprettetOppgave.Companion.EMPTY
import no.nav.aap.rest.AbstractWebClientAdapter
import no.nav.aap.util.LoggerUtil
import no.nav.aap.util.WebClientExtensions.toResponse
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpStatus.*
import org.springframework.http.MediaType.*
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient

@Component
class ArenaWebClientAdapter(@Qualifier(ARENA) webClient : WebClient, val cf : ArenaConfig) : AbstractWebClientAdapter(webClient, cf) {

    private val log = LoggerUtil.getLogger(ArenaWebClientAdapter::class.java)

    fun nyesteArenaSak(fnr : Fødselsnummer) =
        if (cf.oppslagEnabled) {
            webClient.get()
                .uri { cf.nyesteSakUri(it, fnr) }
                .accept(APPLICATION_JSON)
                .exchangeToMono { it.toResponse<String>(log) }
                .retryWhen(cf.retrySpec(log, cf.nyesteSakPath))
                .doOnSuccess { log.info("Oppslag av nyeste oppgave fra Arena OK. Respons $it") }
                .doOnError { t -> log.warn("Oppslag av nyeste oppgave fra Arena feilet (${t.message})", t) }
                .block()
        }
        else {
            log.info("Slo IKKE opp arena sak, set arena.oppslagenabled=true for å aktivere")
            null
        }

    fun opprettArenaOppgave(data : ArenaOpprettOppgaveData, journalpostId : String) =
        if (cf.isEnabled) {
            webClient.post()
                .uri(cf::oppgaveUri)
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .bodyValue(data)
                .exchangeToMono { it.toResponse<ArenaOpprettetOppgave>(log) }
                .retryWhen(cf.retrySpec(log, cf.oppgavePath))
                .doOnSuccess { log.info("Arena opprettet oppgave for journalpost $journalpostId OK. Respons $it") }
                .doOnError { t -> log.warn("Arena opprett oppgave feilet  (${t.message})", t) }
                .block() ?: throw IrrecoverableIntegrationException("Null respons ved opprettelse av oppgave for journalpost $journalpostId")
        }
        else {
            EMPTY.also {
                log.info("Opprettet IKKE arena oppgave for journalpost $journalpostId, set arena.enabled=true for å aktivere")
            }
        }

    override fun toString() = "ArenaWebClientAdapter(cf=$cf), ${super.toString()})"
}