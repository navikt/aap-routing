package no.nav.aap.fordeling.person

import io.github.resilience4j.retry.annotation.Retry
import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.fordeling.graphql.AbstractGraphQLAdapter
import no.nav.aap.fordeling.graphql.AbstractGraphQLAdapter.Companion
import no.nav.aap.fordeling.graphql.AbstractGraphQLAdapter.Companion.GRAPHQL
import org.springframework.stereotype.Component

@Component
class PDLClient(private val a: PDLWebClientAdapter) {
    @Retry(name = GRAPHQL)
    fun geoTilknytning(fnr: Fødselsnummer) = a.geoTilknytning(fnr) ?: throw IllegalStateException("Ingen GT")
    @Retry(name = GRAPHQL)
    fun diskresjonskode(fnr: Fødselsnummer) = a.diskresjonskode(fnr)
}