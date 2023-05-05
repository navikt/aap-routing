package no.nav.aap.fordeling.person

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
class PDLWebClientAdapter(@Qualifier(PDL) val client : WebClient, @Qualifier(PDL) graphQL : GraphQlClient/* @Qualifier(PDL) val graphQL1 : GraphQLWebClient*/,
                          cfg : PDLConfig)
    : AbstractGraphQLAdapter(client, graphQL, cfg) {

    @Retry(name = PDL)
    fun fnr(aktørId : AktørId) = query<Identer>(IDENT, IDENT_PATH, aktørId.asIdent())?.fnr()

    @Retry(name = PDL)
    fun diskresjonskode(fnr : Fødselsnummer) =
        query<PDLAdressebeskyttelse>(BESKYTTELSE, BESKYTTELSE_PATH, fnr.asIdent())?.tilDiskresjonskode() ?: ANY

    @Retry(name = PDL)
    fun geoTilknytning(fnr : Fødselsnummer) = query<PDLGeoTilknytning>(GT, GT_PATH, fnr.asIdent())?.gt()

    override fun toString() = "${javaClass.simpleName} [cfg=$cfg, ${super.toString()}]"

    companion object {

        private fun Fødselsnummer.asIdent() = mapOf(ID to fnr)
        private fun AktørId.asIdent() = mapOf(ID to id)
        private const val ID = "ident"
        private const val BESKYTTELSE = "query-beskyttelse"
        private const val BESKYTTELSE_PATH = "hentPerson"
        private const val GT = "query-gt"
        private const val GT_PATH = "hentGeografiskTilknytning"
        private const val IDENT = "query-ident"
        private const val IDENT_PATH = "hentIdenter"
    }
}