package no.nav.aap.fordeling.person

import no.nav.aap.api.felles.Fødselsnummer
import org.springframework.stereotype.Component

@Component
class PDLClient(private val adapter: PDLWebClientAdapter) {
    fun geoTilknytning(fnr: Fødselsnummer) = adapter.geoTilknytning(fnr) ?: throw IllegalArgumentException("Ingen GT")
    fun diskresjonskode(fnr: Fødselsnummer) = adapter.diskresjonskode(fnr)
}