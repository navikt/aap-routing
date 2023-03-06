package no.nav.aap.fordeling.navenhet

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
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

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class NavOrg(val enhetNr: String, val status: String) {
        data class NAVEnhet(val enhetNr: String)
    }
}