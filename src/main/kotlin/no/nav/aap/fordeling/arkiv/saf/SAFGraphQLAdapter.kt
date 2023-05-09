package no.nav.aap.fordeling.arkiv.saf

import graphql.kickstart.spring.webclient.boot.GraphQLWebClient
import io.github.resilience4j.retry.annotation.Retry
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.MediaType.*
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import no.nav.aap.fordeling.fordeling.FordelingDTOs.JournalpostDTO
import no.nav.aap.fordeling.arkiv.journalpost.JournalpostMapper
import no.nav.aap.fordeling.arkiv.saf.SAFConfig.Companion.SAF
import no.nav.aap.fordeling.graphql.AbstractGraphQLAdapter

@Component
class SAFGraphQLAdapter(@Qualifier(SAF) private val graphQL : GraphQLWebClient, @Qualifier(SAF) webClient : WebClient, private val mapper : JournalpostMapper,
                        cf : SAFConfig) : AbstractGraphQLAdapter(webClient, cf) {

    @Retry(name = SAF)
    fun hentJournalpost(journalpostId : String) =
        query<JournalpostDTO>(graphQL, JOURNALPOST_QUERY, journalpostId.asIdent(), "Journalpost $journalpostId")?.let {
            mapper.tilJournalpost(it)
        }

    fun hentJournalpostRAW(journalpostId : String) = query<JournalpostDTO>(graphQL, JOURNALPOST_QUERY, journalpostId.asIdent())

    override fun toString() = "SAFGraphQLAdapter(graphQL=$graphQL, mapper=$mapper, ${super.toString()})"

    companion object {

        private fun String.asIdent() = mapOf(ID to this)
        private const val JOURNALPOST_QUERY = "query-journalpost.graphql"
        private const val ID = "journalpostId"
    }
}