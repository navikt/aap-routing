package no.nav.aap.fordeling.arkiv.saf

import graphql.kickstart.spring.webclient.boot.GraphQLWebClient
import io.github.resilience4j.retry.annotation.Retry
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.JournalpostDTO
import no.nav.aap.fordeling.arkiv.fordeling.JournalpostMapper
import no.nav.aap.fordeling.arkiv.saf.SAFConfig.Companion.SAF
import no.nav.aap.fordeling.graphql.AbstractGraphQLAdapter
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.MediaType.*
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient

@Component
class SAFGraphQLAdapter(
        @Qualifier(SAF) private val graphQL: GraphQLWebClient,
        @Qualifier(SAF) webClient: WebClient,
        private val mapper: JournalpostMapper,
        cf: SAFConfig) : AbstractGraphQLAdapter(webClient, cf) {

    @Retry(name = SAF)
    fun hentJournalpost(journalpostId: String) =
        query<JournalpostDTO>(graphQL, JOURNALPOST_QUERY, journalpostId.asIdent(),"Journalpost $journalpostId")?.let {
            mapper.tilJournalpost(it)
        }

    fun hentJournalpostRAW(journalpostId: String) =
        query<JournalpostDTO>(graphQL, JOURNALPOST_QUERY, journalpostId.asIdent())

    companion object {
        private fun String.asIdent() = mapOf(ID to this)
        private const val JOURNALPOST_QUERY = "query-journalpost.graphql"
        private const val ID = "journalpostId"
    }
}