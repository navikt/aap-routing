package no.nav.aap.fordeling.person

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.graphql.client.GraphQlClient
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import no.nav.aap.api.felles.AktørId
import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.felles.graphql.AbstractGraphQLAdapter
import no.nav.aap.fordeling.person.Diskresjonskode.ANY
import no.nav.aap.fordeling.person.PDLConfig.Companion.PDL

@Component
class PDLWebClientAdapter(@Qualifier(PDL) webClient : WebClient, @Qualifier(PDL) private val graphQL : GraphQlClient, cfg : PDLConfig) : AbstractGraphQLAdapter(
    webClient,
    cfg) {

    fun fnr(aktørId : AktørId) = query<Identer>(graphQL, IDENT, aktørId.asIdent(), "Aktørid $aktørId")?.fnr()

    fun diskresjonskode(fnr : Fødselsnummer) = query<PDLAdressebeskyttelse>(graphQL, BESKYTTELSE, fnr.asIdent(), "Fnr $fnr")?.tilDiskresjonskode() ?: ANY

    fun geoTilknytning(fnr : Fødselsnummer) = query<PDLGeoTilknytning>(graphQL, GT, fnr.asIdent(), "Fnr $fnr")?.gt()

    override fun toString() = "${javaClass.simpleName} [cfg=$cfg, ${super.toString()}]"

    companion object {

        private fun Fødselsnummer.asIdent() = mapOf(ID to fnr)
        private fun AktørId.asIdent() = mapOf(ID to id)
        private const val ID = "ident"
        private val BESKYTTELSE = Pair("query-beskyttelse", "hentPerson")
        private val GT = Pair("query-gt", "hentGeografiskTilknytning")
        private val IDENT = Pair("query-ident", "hentIdenter")
    }
}