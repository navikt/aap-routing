package no.nav.aap.fordeling.person

import graphql.kickstart.spring.webclient.boot.GraphQLWebClient
import io.github.resilience4j.retry.annotation.Retry
import no.nav.aap.api.felles.AktørId
import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.fordeling.graphql.AbstractGraphQLAdapter
import no.nav.aap.fordeling.person.Diskresjonskode.ANY
import no.nav.aap.fordeling.person.PDLConfig.Companion.PDL
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient

@Component
class PDLWebClientAdapter(
        @Qualifier(PDL) val client: WebClient,
        @Qualifier(PDL) val graphQL: GraphQLWebClient,
        cfg: PDLConfig) : AbstractGraphQLAdapter(client, cfg) {

    @Retry(name = GRAPHQL)
    fun fnr(aktørId: AktørId) = query<Identer>(graphQL, IDENT_QUERY, aktørId.asIdent())?.fnr()

    @Retry(name = GRAPHQL)
    fun diskresjonskode(fnr: Fødselsnummer) =
        query<PDLAdressebeskyttelse>(graphQL, BESKYTTELSE_QUERY, fnr.asIdent())?.tilDiskresjonskode() ?: ANY

    @Retry(name = GRAPHQL)
    fun geoTilknytning(fnr: Fødselsnummer) = query<PDLGeoTilknytning>(graphQL, GT_QUERY, fnr.asIdent())?.gt()
    private fun Fødselsnummer.asIdent() = mapOf(IDENT to fnr)

    private fun AktørId.asIdent() = mapOf(IDENT to id)


    override fun toString() =
        "${javaClass.simpleName} [graphQL=$graphQL,webClient=$client, cfg=$cfg]"

    companion object {
        private const val IDENT = "ident"
        private const val BESKYTTELSE_QUERY = "query-beskyttelse.graphql"
        private const val GT_QUERY = "query-gt.graphql"
        private const val IDENT_QUERY = "query-ident.graphql"

    }
}