package no.nav.aap.fordeling.arkiv.dokarkiv

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.http.MediaType.TEXT_PLAIN
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import no.nav.aap.api.felles.error.IrrecoverableIntegrationException
import no.nav.aap.fordeling.arkiv.dokarkiv.DokarkivConfig.Companion.DOKARKIV
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.JournalpostDTO.OppdateringDataDTO
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.JournalpostDTO.OppdateringDataDTO.SakDTO
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.JournalpostDTO.OppdateringResponsDTO
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.JournalpostDTO.OppdateringResponsDTO.Companion.EMPTY
import no.nav.aap.fordeling.arkiv.fordeling.Journalpost
import no.nav.aap.fordeling.arkiv.fordeling.JournalpostMapper.Companion.toDTO
import no.nav.aap.fordeling.navenhet.NAVEnhet.Companion.AUTO_ENHET
import no.nav.aap.rest.AbstractWebClientAdapter
import no.nav.aap.util.LoggerUtil
import no.nav.aap.util.WebClientExtensions.response

@Component
class DokarkivWebClientAdapter(@Qualifier(DOKARKIV) webClient : WebClient, val cf : DokarkivConfig,
                               private val mapper : ObjectMapper) : AbstractWebClientAdapter(webClient, cf) {

    private val log = LoggerUtil.getLogger(DokarkivWebClientAdapter::class.java)

    fun oppdaterOgFerdigstillJournalpost(jp : Journalpost, saksNr : String) =
        with(jp) {
            oppdaterJournalpost(this, saksNr)
            ferdigstillJournalpost(this.id)
        }

    fun oppdaterJournalpost(jp : Journalpost, saksNr : String) =
        if (cf.isEnabled) {
            webClient.put()
                .uri { cf.oppdaterJournlpostUri(it, jp.id) }
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .bodyValue(jp.oppdateringsData(saksNr))
                .exchangeToMono { it.response<OppdateringResponsDTO>(log) }
                .retryWhen(cf.retrySpec(log, cf.oppdaterPath))
                .doOnSuccess { log.info("Oppdatering av journalpost ${jp.id} OK. Respons $it") }
                .contextCapture()
                .block() ?: IrrecoverableIntegrationException("Null respons fra dokarkiv ved oppdatering av journalpost ${jp.id}")
        }
        else {
            EMPTY.also {
                log.info("Oppdaterte ikke journalpost ${jp.id}, sett dokarkiv.enabled=true for  aktivere")
            }
        }

    fun ferdigstillJournalpost(journalpostId : String) =
        if (cf.isEnabled) {
            webClient.patch()
                .uri { cf.ferdigstillUri(it, journalpostId) }
                .contentType(APPLICATION_JSON)
                .accept(TEXT_PLAIN)
                .bodyValue(AUTO_ENHET)
                .exchangeToMono { it.response<String>(log) }
                .retryWhen(cf.retrySpec(log, cf.ferdigstillPath))
                .doOnSuccess { log.info("Ferdigstilling av journalpost OK. Respons $it") }
                .doOnError { t -> log.warn("Ferdigstilling av journalpost $journalpostId feilet", t) }
                .contextCapture()
                .block() ?: IrrecoverableIntegrationException("Null respons fra dokarkiv ved ferdigstilling av journalpost $journalpostId")
        }
        else {
            log.info("Ferdigstilte ikke journalpost $journalpostId, sett dokarkiv.enabled=true for  aktivere")
            "Ingen ferdigstiling"
        }

    private fun Journalpost.oppdateringsData(saksNr : String) =
        OppdateringDataDTO(tittel, avsenderMottager?.toDTO() ?: bruker?.toDTO(), bruker?.toDTO(), SakDTO(saksNr), tema.uppercase())

    override fun toString() = "DokarkivWebClientAdapter(cf=$cf, mapper=$mapper), ${super.toString()})"
}