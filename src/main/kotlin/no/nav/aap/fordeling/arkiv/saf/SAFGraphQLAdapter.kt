package no.nav.aap.fordeling.arkiv.saf

import io.github.resilience4j.retry.annotation.Retry
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.graphql.client.GraphQlClient
import org.springframework.http.MediaType.*
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.JournalpostDTO
import no.nav.aap.fordeling.arkiv.fordeling.JournalpostMapper
import no.nav.aap.fordeling.arkiv.saf.SAFConfig.Companion.SAF
import no.nav.aap.fordeling.graphql.AbstractGraphQLAdapter

@Component
class SAFGraphQLAdapter(@Qualifier(SAF) graphQL : GraphQlClient,
                        @Qualifier(SAF) webClient : WebClient,
                        private val mapper : JournalpostMapper,
                        cf : SAFConfig) : AbstractGraphQLAdapter(webClient, graphQL, cf) {

    @Retry(name = SAF)
    fun hentJournalpost(id : String) =
        query<JournalpostDTO>(JP, JP_PATH, id.asIdent(), "Journalpost $id")?.let {
            mapper.tilJournalpost(it)
        }

    override fun toString() = "SAFGraphQLAdapter(mapper=$mapper, ${super.toString()})"

    companion object {

        private fun String.asIdent() = mapOf(ID to this)
        private const val JP = "query-journalpost"
        private const val JP_PATH = "journalpost"
        private const val ID = "journalpostId"
    }
}