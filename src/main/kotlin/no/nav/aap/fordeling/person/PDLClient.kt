package no.nav.aap.fordeling.person

import no.nav.aap.api.felles.Fødselsnummer
import org.springframework.stereotype.Component

@Component
class PDLClient(private val a: PDLWebClientAdapter) {
    fun geoTilknytning(fnr: Fødselsnummer) = a.geoTilknytning(fnr) ?: throw IllegalStateException("Ingen GT")
    fun diskresjonskode(fnr: Fødselsnummer) = a.diskresjonskode(fnr)
}