package no.nav.aap.fordeling.arkiv.saf

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.MediaType.*
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.DokumentVariant.VariantFormat
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.DokumentVariant.VariantFormat.ORIGINAL
import no.nav.aap.fordeling.arkiv.fordeling.Journalpost
import no.nav.aap.fordeling.arkiv.saf.SAFConfig.Companion.SAFDOK
import no.nav.aap.rest.AbstractWebClientAdapter
import no.nav.aap.util.WebClientExtensions.response

@Component
class SAFWebClientAdapter(@Qualifier(SAFDOK) webClient : WebClient, private val cf : SAFConfig) : AbstractWebClientAdapter(webClient, cf) {

    fun originalDokument(jp : Journalpost) = dokument(jp.id, jp.hovedDokument.id, ORIGINAL)?.let(::String)

    private fun dokument(id : String, dokumentId : String, variantFormat : VariantFormat) =
        webClient.get()
            .uri { cf.dokUri(it, id, dokumentId, variantFormat) }
            .accept(APPLICATION_JSON)
            .exchangeToMono { it.response<ByteArray>(log) }
            .retryWhen(cf.retrySpec(log, cf.dokPath))
            .doOnError { t -> log.warn("Arkivoppslag feilet for  $id/$dokumentId/$variantFormat", t) }
            .doOnSuccess { log.info("Arkivoppslag $id/$dokumentId/$variantFormat returnerte ${it.size} bytes") }
            .contextCapture()
            .block()
}