package no.nav.aap.fordeling.person

import io.micrometer.observation.annotation.Observed
import org.springframework.stereotype.Component
import no.nav.aap.api.felles.AktørId
import no.nav.aap.api.felles.Fødselsnummer

@Component
@Observed(contextualName = "PDL")
class PDLClient(private val a : PDLWebClientAdapter) {

    fun geoTilknytning(fnr : Fødselsnummer) = a.geoTilknytning1(fnr)

    fun diskresjonskode(fnr : Fødselsnummer) = a.diskresjonskode(fnr)

    fun fnr(id : AktørId) = a.fnr(id)
}