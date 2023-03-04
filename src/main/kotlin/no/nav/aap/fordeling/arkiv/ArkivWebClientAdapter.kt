package no.nav.aap.fordeling.arkiv

import no.nav.aap.fordeling.arkiv.ArkivConfig.Companion.DOKARKIV
import no.nav.aap.fordeling.arkiv.JournalpostDTO.JournalførendeEnhet.Companion.AUTOMATISK_JOURNALFØRING
import no.nav.aap.fordeling.arkiv.JournalpostDTO.OppdaterForespørsel
import no.nav.aap.fordeling.arkiv.JournalpostDTO.OppdaterRespons
import no.nav.aap.fordeling.arkiv.JournalpostDTO.OppdaterRespons.Companion.EMPTY
import no.nav.aap.rest.AbstractWebClientAdapter
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.MediaType.*
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

@Component
class ArkivWebClientAdapter( @Qualifier(DOKARKIV) private val dokarkiv: WebClient, val cf: ArkivConfig) :
    AbstractWebClientAdapter(dokarkiv, cf) {

    fun oppdaterOgFerdigstillJournalpost(journalpostId: String, data: OppdaterForespørsel) =
        with(journalpostId) {
            oppdaterJournalpost(this, data)
            ferdigstillJournalpost(this)
        }

    fun oppdaterJournalpost(journalpostId: String, data: OppdaterForespørsel) =
        if (cf.isEnabled) {
            dokarkiv.put()
                .uri { b -> b.path(cf.oppdaterPath).build(journalpostId) }
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .bodyValue(data)
                .retrieve()
                .bodyToMono<OppdaterRespons>()
                .retryWhen(cf.retrySpec(log))
                .doOnSuccess { log.info("Oppdatering av journalpost $journalpostId med $data OK. Respons $it") }
                .doOnError { t -> log.warn("Oppdatering av journalpost $journalpostId med $data feilet", t) }
                .block()
        }
        else {
            EMPTY.also {
                log.info("Oppdaterte ikke journalpost med $data")
            }
        }

    fun ferdigstillJournalpost(journalpostId: String) =
        if (cf.isEnabled) {
            dokarkiv.patch()
            .uri { b -> b.path(cf.ferdigstillPath).build(journalpostId) }
            .contentType(APPLICATION_JSON)
            .accept(TEXT_PLAIN)
            .bodyValue(AUTOMATISK_JOURNALFØRING)
            .retrieve()
            .bodyToMono<String>()
            .retryWhen(cf.retrySpec(log))
            .doOnSuccess { log.info("Ferdigstilling av journalpost OK. Respons $it") }
            .doOnError { t -> log.warn("Ferdigstilling av journalpost $journalpostId feilet", t) }
            .block()
        }
        else {
            "Ferdigstilte ikke journalpost $journalpostId".also {
                log.info(it)
            }
        }
}