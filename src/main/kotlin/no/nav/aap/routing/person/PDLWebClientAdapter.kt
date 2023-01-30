package no.nav.aap.routing.person

import graphql.kickstart.spring.webclient.boot.GraphQLWebClient
import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.routing.arkiv.graphql.AbstractGraphQLAdapter
import no.nav.aap.routing.person.PDLConfig.Companion.PDL
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.http.MediaType.TEXT_PLAIN
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient


@Component
class PDLWebClientAdapter(@Qualifier(PDL) val client: WebClient, @Qualifier(PDL) val graphQL: GraphQLWebClient, cfg: PDLConfig) : AbstractGraphQLAdapter(client, cfg) {

    override fun ping() =
        client
            .options()
            .uri(baseUri)
            .accept(APPLICATION_JSON, TEXT_PLAIN)
            .retrieve()
            .toBodilessEntity()
            .block().run { emptyMap<String,String>() }

    fun gt(fnr: Fødselsnummer) = query<Map<String,Any>>(graphQL,GT_QUERY, fnr.asIdent()) // todo map to domain

    private fun Fødselsnummer.asIdent() = mapOf("ident" to fnr)

    override fun toString() =
        "${javaClass.simpleName} [graphQL=$graphQL,webClient=$client, cfg=$cfg]"

    companion object {
        private const val GT_QUERY = "query-gt.graphql"
    }
}