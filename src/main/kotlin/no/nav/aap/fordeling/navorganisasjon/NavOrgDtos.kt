package no.nav.aap.fordeling.navorganisasjon

import no.nav.aap.fordeling.person.Diskresjonskode
import no.nav.aap.fordeling.person.Diskresjonskode.ANY
import no.nav.aap.util.Constants.AAP

data class EnhetsKriteria(val geografiskOmraade: String,
                          val skjermet: Boolean = false,
                          val diskresjonskode: Diskresjonskode = ANY,
                          val tema: String = AAP.uppercase()) {

    enum class Status {
        AKTIV, UNDER_ETABLERING,
        UNDER_AVVIKLING,
        NEDLAGT
    }

}