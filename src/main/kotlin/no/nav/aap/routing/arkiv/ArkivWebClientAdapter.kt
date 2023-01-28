package no.nav.aap.routing.arkiv

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import graphql.kickstart.spring.webclient.boot.GraphQLWebClient
import no.nav.aap.api.felles.error.IntegrationException
import no.nav.aap.rest.AbstractWebClientAdapter
import no.nav.aap.routing.arkiv.graphql.AbstractGraphQLAdapter
import no.nav.aap.routing.arkiv.graphql.GraphQLDefaultErrorHandler
import no.nav.aap.util.Constants.JOARK
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpStatus.*
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono

@Component
class ArkivWebClientAdapter(@Qualifier(JOARK) private val graphQL: GraphQLWebClient, @Qualifier(JOARK) webClient: WebClient, val cf: ArkivConfig) :
    AbstractGraphQLAdapter(webClient, cf) {


    fun journalpost(journalpost: String) = query<Map<String,Any>>(graphQL, JOURNALPOST_QUERY, mapOf(ID to journalpost))

    companion object {
        private const val JOURNALPOST_QUERY = "query-journalpost.graphql"
        private const val ID = "journalpostId"
    }
}