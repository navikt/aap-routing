package no.nav.aap.routing.navorganisasjon

import no.nav.aap.routing.navorganisasjon.EnhetsKriteria.Diskresjonskode.ANY
import no.nav.aap.routing.navorganisasjon.EnhetsKriteria.Diskresjonskode.values
import no.nav.aap.util.Constants.AAP

data class EnhetsKriteria(val geografiskOmraade: String,
                          val skjermet: Boolean = false,
                          val diskresjonskode: Diskresjonskode = ANY,
                          val tema: String = AAP.uppercase()) {

    enum class Diskresjonskode(vararg val koder: String) {

        SPFO("FORTROLIG"), SPSF("STRENGT_FORTROLIG","STRENGT_FORTROLIG_UTLAND"), ANY;
        companion object{
            fun of(kode: String) = values().firstOrNull { kode in it.koder } ?: ANY
        }
    }
}