package no.nav.aap.fordeling.arkiv.saf

import graphql.kickstart.spring.webclient.boot.GraphQLWebClient
import io.github.resilience4j.retry.annotation.Retry
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.MediaType.*
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import no.nav.aap.api.felles.error.IrrecoverableIntegrationException
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.DokumentVariant.VariantFormat
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.DokumentVariant.VariantFormat.ORIGINAL
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.JournalpostDTO
import no.nav.aap.fordeling.arkiv.fordeling.Journalpost
import no.nav.aap.fordeling.arkiv.fordeling.JournalpostMapper
import no.nav.aap.fordeling.arkiv.saf.SAFConfig.Companion.SAF
import no.nav.aap.fordeling.graphql.AbstractGraphQLAdapter
import no.nav.aap.util.WebClientExtensions.toResponse

@Component
class SAFGraphQLAdapter(@Qualifier(SAF) private val graphQL : GraphQLWebClient, @Qualifier(SAF) webClient : WebClient, private val mapper : JournalpostMapper,
                        private val cf : SAFConfig) : AbstractGraphQLAdapter(webClient, cf) {

    @Retry(name = SAF)
    fun hentJournalpost(journalpostId : String) =
        query<JournalpostDTO>(graphQL, JOURNALPOST_QUERY, journalpostId.asIdent(), "Journalpost $journalpostId")?.let {
            mapper.tilJournalpost(it)
        }

    fun hentJournalpostRAW(journalpostId : String) =
        query<JournalpostDTO>(graphQL, JOURNALPOST_QUERY, journalpostId.asIdent())

    fun søknad(jp : Journalpost) = dokument(jp.id, jp.hovedDokument.id, ORIGINAL)

    private fun dokument(id : String, dokumentId : String, variantFormat : VariantFormat) =
        webClient.get()
            .uri { cf.dokUri(it, id, dokumentId, variantFormat) }
            .accept(APPLICATION_JSON)
            .exchangeToMono { it.toResponse<ByteArray>(log) }
            .retryWhen(cf.retrySpec(log, cf.dokPath))
            .doOnError { t -> log.warn("Arkivoppslag feilet", t) }
            .doOnSuccess { log.trace("Arkivoppslag $dokumentId returnerte  ${it.size} bytes") }
            .block()
            ?.map { ::String }
            ?: IrrecoverableIntegrationException("Null respons fra dokarkiv ved henting av dokument $dokumentId")

    override fun toString() = "SAFGraphQLAdapter(graphQL=$graphQL, mapper=$mapper, , ${super.toString()})"

    companion object {

        private fun String.asIdent() = mapOf(ID to this)
        private const val JOURNALPOST_QUERY = "query-journalpost.graphql"
        private const val ID = "journalpostId"
    }
}