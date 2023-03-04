package no.nav.aap.fordeling.arkiv.saf

import graphql.kickstart.spring.webclient.boot.GraphQLWebClient
import no.nav.aap.fordeling.arkiv.fordeling.JournalpostDTO
import no.nav.aap.fordeling.arkiv.saf.SafConfig.Companion.SAF
import no.nav.aap.fordeling.graphql.AbstractGraphQLAdapter
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.MediaType.*
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient

@Component
class SafGraphQLAdapter(@Qualifier(SAF) private val graphQL: GraphQLWebClient, @Qualifier(SAF) webClient: WebClient, cf: SafConfig) :
    AbstractGraphQLAdapter(webClient, cf) {

    fun hentJournalpost(journalpostId: Long) = query<JournalpostDTO>(graphQL, JOURNALPOST_QUERY, journalpostId.asIdent())?.tilJournalpost()

    override fun ping() =
        webClient
            .options()
            .uri(baseUri)
            .accept(APPLICATION_JSON, TEXT_PLAIN)
            .retrieve()
            .toBodilessEntity()
            .block().run { emptyMap<String,String>() }
    companion object {
        private fun Long.asIdent() = mapOf(ID to "$this")
        private const val JOURNALPOST_QUERY = "query-journalpost.graphql"
        private const val ID = "journalpostId"
    }
}