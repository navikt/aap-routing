package no.nav.aap.fordeling.person

import org.springframework.stereotype.Component
import no.nav.aap.api.felles.AktørId
import no.nav.aap.api.felles.Fødselsnummer

@Component
class PDLClient(private val a : PDLWebClientAdapter) {

    fun geoTilknytning(fnr : Fødselsnummer) = a.geoTilknytning(fnr)

    fun diskresjonskode(fnr : Fødselsnummer) = a.diskresjonskode(fnr)

    fun fnr(id : AktørId) = a.fnr(id)
}