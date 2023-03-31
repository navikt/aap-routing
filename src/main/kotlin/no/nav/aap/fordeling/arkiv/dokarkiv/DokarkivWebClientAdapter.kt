package no.nav.aap.fordeling.arkiv.dokarkiv

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.http.MediaType.TEXT_PLAIN
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import no.nav.aap.api.felles.error.IrrecoverableIntegrationException
import no.nav.aap.fordeling.arkiv.dokarkiv.DokarkivConfig.Companion.DOKARKIV
import no.nav.aap.fordeling.arkiv.dokarkiv.DokarkivWebClientAdapter.VariantFormat.JSON
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.JournalpostDTO.OppdateringData
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.JournalpostDTO.OppdateringRespons
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.JournalpostDTO.OppdateringRespons.Companion.EMPTY
import no.nav.aap.fordeling.arkiv.fordeling.Journalpost
import no.nav.aap.fordeling.navenhet.NAVEnhet.Companion.AUTOMATISK_JOURNALFØRING_ENHET
import no.nav.aap.rest.AbstractWebClientAdapter
import no.nav.aap.util.LoggerUtil
import no.nav.aap.util.WebClientExtensions.toResponse

@Component
class DokarkivWebClientAdapter(@Qualifier(DOKARKIV) webClient : WebClient, val cf : DokarkivConfig,
                               private val mapper : ObjectMapper) : AbstractWebClientAdapter(webClient, cf) {

    private val log = LoggerUtil.getLogger(DokarkivWebClientAdapter::class.java)

    fun oppdaterOgFerdigstillJournalpost(journalpostId : String, data : OppdateringData) =
        with(journalpostId) {
            oppdaterJournalpost(this, data)
            ferdigstillJournalpost(this)
        }

    fun oppdaterJournalpost(journalpostId : String, data : OppdateringData) =
        if (cf.isEnabled) {
            webClient.put()
                .uri { cf.oppdaterJournlpostUri(it, journalpostId) }
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .bodyValue(data)
                .exchangeToMono { it.toResponse<OppdateringRespons>(log) }
                .retryWhen(cf.retrySpec(log, cf.oppdaterPath))
                .doOnSuccess { log.info("Oppdatering av journalpost $journalpostId fra $data OK. Respons $it") }
                .doOnError { t -> log.warn("Oppdatering av journalpost $journalpostId fra $data feilet", t) }
                .block() ?: IrrecoverableIntegrationException("Null respons fra dokarkiv ved oppdatering av journalpost $journalpostId")
        }
        else {
            EMPTY.also {
                log.info("Oppdaterte ikke journalpost $journalpostId, sett dokarkiv.enabled=true for  aktivere")
            }
        }

    fun ferdigstillJournalpost(journalpostId : String) =
        if (cf.isEnabled) {
            webClient.patch()
                .uri { cf.ferdigstillUri(it, journalpostId) }
                .contentType(APPLICATION_JSON)
                .accept(TEXT_PLAIN)
                .bodyValue(AUTOMATISK_JOURNALFØRING_ENHET)
                .exchangeToMono { it.toResponse<String>(log) }
                .retryWhen(cf.retrySpec(log, cf.ferdigstillPath))
                .doOnSuccess { log.info("Ferdigstilling av journalpost OK. Respons $it") }
                .doOnError { t -> log.warn("Ferdigstilling av journalpost $journalpostId feilet", t) }
                .block() ?: IrrecoverableIntegrationException("Null respons fra dokarkiv ved ferdigstilling av journalpost $journalpostId")
        }
        else {
            log.info("Ferdigstilte ikke journalpost $journalpostId, sett dokarkiv.enabled=true for  aktivere")
            "Ingen ferdigstiling"
        }

    fun søknad(jp : Journalpost) = dokument(jp.journalpostId, søknadDokumentId(jp), JSON)

    fun dokument(journalpostId : String, dokumentInfoId : String, variantFormat : VariantFormat) =
        webClient.get()
            .uri { cf.dokUri(it, journalpostId, dokumentInfoId, variantFormat) }
            .accept(APPLICATION_JSON)
            .exchangeToMono { it.toResponse<ByteArray>(log) }
            .retryWhen(cf.retrySpec(log, cf.dokPath))
            .doOnSuccess { log.trace("Arkivoppslag returnerte  ${it.size} bytes") }
            .block() ?: IrrecoverableIntegrationException("Null respons fra dokarkiv ved henting av journalpost $journalpostId")

    private fun søknadDokumentId(jp : Journalpost) = jp.dokumenter.first().dokumentInfoId
    override fun toString() = "DokarkivWebClientAdapter(cf=$cf, mapper=$mapper), ${super.toString()})"

    enum class VariantFormat { JSON, ARKIV }
}