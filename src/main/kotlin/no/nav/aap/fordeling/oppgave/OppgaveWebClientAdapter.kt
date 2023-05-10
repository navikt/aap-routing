package no.nav.aap.fordeling.oppgave

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import no.nav.aap.api.felles.error.IrrecoverableIntegrationException
import no.nav.aap.fordeling.arkiv.journalpost.Journalpost
import no.nav.aap.fordeling.oppgave.OppgaveConfig.Companion.OPPGAVE
import no.nav.aap.fordeling.oppgave.OppgaveDTOs.OppgaveRespons
import no.nav.aap.rest.AbstractWebClientAdapter
import no.nav.aap.util.LoggerUtil
import no.nav.aap.util.WebClientExtensions.response

@Component
class OppgaveWebClientAdapter(@Qualifier(OPPGAVE) webClient : WebClient, val cf : OppgaveConfig) :
    AbstractWebClientAdapter(webClient, cf) {

    private val log = LoggerUtil.getLogger(OppgaveWebClientAdapter::class.java)

    fun harOppgave(journalpostId : String) =
        if (cf.oppslagEnabled) {
            webClient.get()
                .uri { cf.oppgaveUri(it, journalpostId) }
                .exchangeToMono { it.response<OppgaveRespons>(log) }
                .retryWhen(cf.retrySpec(log, object {}.javaClass.enclosingMethod.name.lowercase()))
                .doOnSuccess { log.trace("Oppgave oppslag journalpost  {} OK. Respons {}", journalpostId, it) }
                .doOnError { log.warn("Oppgave oppslag journalpost  $journalpostId feilet (${it.message})", it) }
                .contextCapture()
                .block()?.antallTreffTotalt?.let { it > 0 }
                ?: throw IrrecoverableIntegrationException("Null respons fra opslag oppgave $journalpostId")
        }
        else {
            log.info("Slår IKKE opp oppgave")
            false
        }

    fun opprettOppgave(jp : Journalpost, oppgaveType : OppgaveType, enhetNr : String? = null) =
        if (cf.isEnabled) {
            webClient.post()
                .uri(cf::opprettOppgaveUri)
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .bodyValue(jp.opprettOppgaveData(oppgaveType, enhetNr))
                .exchangeToMono { it.response<Any>(log) }
                .retryWhen(cf.retrySpec(log, object {}.javaClass.enclosingMethod.name.lowercase()))
                .doOnSuccess { log.trace("Opprett oppgave fra {} OK. Respons {}", oppgaveType, it) }
                .doOnError { log.warn("Opprett oppgave fra $oppgaveType feilet (${it.message})", it) }
                .contextCapture()
                .block() ?: throw IrrecoverableIntegrationException("Null respons fra opprett oppgave fra $oppgaveType")
        }
        else {
            log.info("Oppretter IKKE oppgave for data $oppgaveType")
        }

    private fun Journalpost.opprettOppgaveData(oppgaveType : OppgaveType, enhetNr : String?) =
        OpprettOppgaveData(fnr.fnr, id, behandlingstema, enhetNr, tittel, oppgaveType.verdi, tema.uppercase())

    override fun toString() = "OppgaveWebClientAdapter(cf=$cf), ${super.toString()})"
}