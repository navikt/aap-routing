package no.nav.aap.fordeling.arkiv

import graphql.kickstart.spring.webclient.boot.GraphQLWebClient
import no.nav.aap.fordeling.arkiv.graphql.AbstractGraphQLAdapter
import no.nav.aap.util.Constants.JOARK
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpStatus.*
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient

@Component
class ArkivWebClientAdapter(@Qualifier(JOARK) private val graphQL: GraphQLWebClient, @Qualifier(JOARK) webClient: WebClient, val cf: ArkivConfig) :
    AbstractGraphQLAdapter(webClient, cf) {

    fun journalpost(journalpost: Long) = query<JournalpostDTO>(graphQL, JOURNALPOST_QUERY, journalpost.asIdent())?.tilJournalpost()

    fun finalizeJournalpost(journalpostId: String): Nothing = TODO()
    fun updateJournalpost(journalpost: Journalpost): Nothing = TODO()

    private fun Long.asIdent() = mapOf(ID to "$this")

    companion object {
        private const val JOURNALPOST_QUERY = "query-journalpost.graphql"
        private const val ID = "journalpostId"
    }
}