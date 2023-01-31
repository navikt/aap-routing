package no.nav.aap.routing.navorganisasjon

import no.nav.aap.routing.navorganisasjon.EnhetsKriteria.Diskresjonskode.ANY
import no.nav.aap.util.Constants.AAP

data class EnhetsKriteria(val geografiskOmraade: String,
                          val skjermet: Boolean = false,
                          val diskresjonskode: Diskresjonskode = ANY,
                          val tema: String = AAP) {

    enum class Diskresjonskode {
        SPFO, SPSF, ANY
    }
}