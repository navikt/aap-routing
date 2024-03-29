package no.nav.aap.fordeling.arena

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpStatus.*
import org.springframework.http.MediaType.*
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.felles.SkjemaType.*
import no.nav.aap.api.felles.error.IrrecoverableIntegrationException
import no.nav.aap.fordeling.arena.ArenaConfig.Companion.ARENA
import no.nav.aap.fordeling.arena.ArenaDTOs.ArenaOpprettOppgaveData
import no.nav.aap.fordeling.arena.ArenaDTOs.ArenaOpprettetOppgave
import no.nav.aap.fordeling.arena.ArenaDTOs.ArenaOpprettetOppgave.Companion.EMPTY
import no.nav.aap.fordeling.arkiv.journalpost.Journalpost
import no.nav.aap.rest.AbstractWebClientAdapter
import no.nav.aap.util.LoggerUtil
import no.nav.aap.util.WebClientExtensions.response

@Component
class ArenaWebClientAdapter(@Qualifier(ARENA) webClient : WebClient, val cf : ArenaConfig) : AbstractWebClientAdapter(webClient, cf) {

    private val log = LoggerUtil.getLogger(ArenaWebClientAdapter::class.java)

    fun nyesteArenaSak(fnr : Fødselsnummer) =
        if (cf.oppslagEnabled) {
            webClient.get()
                .uri { cf.nyesteSakUri(it, fnr) }
                .accept(APPLICATION_JSON)
                .exchangeToMono { it.response<String>() }
                .retryWhen(cf.retrySpec(log, cf.nyesteSakPath))
                .doOnSuccess { log.trace("Oppslag av nyeste oppgave fra Arena OK. Respons $it") }
                .doOnError { log.warn("Oppslag av nyeste oppgave fra Arena feilet (${it.message})", it) }
                .contextCapture()
                .block()
        }
        else {
            log.info("Slo IKKE opp arena sak, set arena.oppslagenabled=true for å aktivere")
            null
        }

    fun opprettArenaOppgave(jp : Journalpost, enhetNr : String) =
        if (cf.isEnabled) {
            webClient.post()
                .uri(cf::oppgaveUri)
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .bodyValue(jp.opprettArenaOppgaveData(enhetNr))
                .exchangeToMono { it.response<ArenaOpprettetOppgave>() }
                .retryWhen(cf.retrySpec(log, cf.oppgavePath))
                .doOnSuccess { log.trace("Arena opprettet oppgave for journalpost {} OK. Respons {}", jp.id, it) }
                .doOnError { log.warn("Arena opprett oppgave feilet  (${it.message})", it) }
                .block() ?: throw IrrecoverableIntegrationException("Null respons ved opprettelse av oppgave for journalpost ${jp.id}")
        }
        else {
            EMPTY.also {
                log.info("Opprettet IKKE arena oppgave for journalpost ${jp.id}, set arena.enabled=true for å aktivere")
            }
        }

    private fun Journalpost.opprettArenaOppgaveData(enhetNr : String) = ArenaOpprettOppgaveData(fnr, enhetNr, hovedDokumentTittel, vedleggTitler)

    override fun toString() = "ArenaWebClientAdapter(cf=$cf), ${super.toString()})"
}