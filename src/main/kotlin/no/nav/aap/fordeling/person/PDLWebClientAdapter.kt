package no.nav.aap.fordeling.person

import graphql.kickstart.spring.webclient.boot.GraphQLWebClient
import io.github.resilience4j.retry.annotation.Retry
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.graphql.client.GraphQlClient
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import no.nav.aap.api.felles.AktørId
import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.fordeling.graphql.AbstractGraphQLAdapter
import no.nav.aap.fordeling.person.Diskresjonskode.ANY
import no.nav.aap.fordeling.person.PDLConfig.Companion.PDL

@Component
class PDLWebClientAdapter(@Qualifier(PDL) val client : WebClient, @Qualifier(PDL) val graphQL : GraphQLWebClient,
                          @Qualifier(PDL) val springGraphQL : GraphQlClient, cfg : PDLConfig)
    : AbstractGraphQLAdapter(client, cfg) {

    @Retry(name = PDL)
    fun fnr(aktørId : AktørId) = query<Identer>(graphQL, IDENT_QUERY, aktørId.asIdent())?.fnr()

    @Retry(name = PDL)
    fun diskresjonskode(fnr : Fødselsnummer) = query<PDLAdressebeskyttelse>(graphQL, BESKYTTELSE_QUERY, fnr.asIdent())?.tilDiskresjonskode() ?: ANY

    @Retry(name = PDL)
    fun geoTilknytning(fnr : Fødselsnummer) = query<PDLGeoTilknytning>(graphQL, GT_QUERY, fnr.asIdent())?.gt()

    fun geoTilknytning1(fnr : Fødselsnummer) = query<PDLGeoTilknytning>(springGraphQL, GT, "hentGeografiskTilknytning", fnr.asIdent())?.gt()

    override fun toString() = "${javaClass.simpleName} [graphQL=$graphQL,webClient=$client, cfg=$cfg, ${super.toString()}]"

    companion object {

        private fun Fødselsnummer.asIdent() = mapOf(IDENT to fnr)
        private fun AktørId.asIdent() = mapOf(IDENT to id)
        private const val IDENT = "ident"
        private const val BESKYTTELSE_QUERY = "query-beskyttelse.graphql"
        private const val GT_QUERY = "query-gt.graphql"
        private const val GT = "query-gt"
        private const val IDENT_QUERY = "query-ident.graphql"
    }
}