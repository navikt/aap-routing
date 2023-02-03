package no.nav.aap.routing.person

import graphql.kickstart.spring.webclient.boot.GraphQLWebClient
import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.routing.arkiv.graphql.AbstractGraphQLAdapter
import no.nav.aap.routing.person.Diskresjonskode.ANY
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

    fun diskresjonskode(fnr: Fødselsnummer) = query<PDLAdressebeskyttelse>(graphQL,BESKYTTELSE_QUERY, fnr.asIdent())?.tilDiskresjonskode() ?: ANY

    fun geoTilknytning(fnr: Fødselsnummer) = query<PDLGeoTilknytning>(graphQL, GT_QUERY, fnr.asIdent())?.gt()
    private fun Fødselsnummer.asIdent() = mapOf("ident" to fnr)

    override fun toString() =
        "${javaClass.simpleName} [graphQL=$graphQL,webClient=$client, cfg=$cfg]"

    companion object {
        private const val BESKYTTELSE_QUERY = "query-beskyttelse.graphql"
        private const val GT_QUERY = "query-gt.graphql"
    }
}
@Component
class PDLClient(private val adapter: PDLWebClientAdapter) {
    fun geoTilknytning(fnr: Fødselsnummer) = adapter.geoTilknytning(fnr) ?: throw IllegalArgumentException("Ingen GT")
    fun diskresjonskode(fnr: Fødselsnummer) = adapter.diskresjonskode(fnr)
}