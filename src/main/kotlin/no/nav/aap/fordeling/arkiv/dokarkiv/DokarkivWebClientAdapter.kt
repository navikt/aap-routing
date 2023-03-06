package no.nav.aap.fordeling.arkiv.dokarkiv

import no.nav.aap.fordeling.arkiv.dokarkiv.DokarkivConfig.Companion.DOKARKIV
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.JournalpostDTO.JournalførendeEnhet.Companion.AUTOMATISK_JOURNALFØRING
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.JournalpostDTO.OppdateringData
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.JournalpostDTO.OppdateringRespons
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.JournalpostDTO.OppdateringRespons.Companion.EMPTY
import no.nav.aap.rest.AbstractWebClientAdapter
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.MediaType.*
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

@Component
class DokarkivWebClientAdapter(@Qualifier(DOKARKIV)  webClient: WebClient, val cf: DokarkivConfig) :
    AbstractWebClientAdapter(webClient, cf) {

    fun oppdaterOgFerdigstillJournalpost(journalpostId: String, data: OppdateringData) =
        with(journalpostId) {
            oppdaterJournalpost(this, data)
            ferdigstillJournalpost(this)
        }

    fun oppdaterJournalpost(journalpostId: String, data: OppdateringData) =
        if (cf.isEnabled) {
            webClient.put()
                .uri { b -> b.path(cf.oppdaterPath).build(journalpostId) }
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .bodyValue(data)
                .retrieve()
                .bodyToMono<OppdateringRespons>()
                .retryWhen(cf.retrySpec(log))
                .doOnSuccess { log.info("Oppdatering av journalpost $journalpostId fra $data OK. Respons $it") }
                .doOnError { t -> log.warn("Oppdatering av journalpost $journalpostId fra $data feilet", t) }
                .block()
        }
        else {
            EMPTY.also {
                log.info("Oppdaterte ikke journalpost med $data")
            }
        }

    fun ferdigstillJournalpost(journalpostId: String) =
        if (cf.isEnabled) {
            webClient.patch()
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