package no.nav.aap.fordeling.navenhet

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import no.nav.aap.fordeling.person.Diskresjonskode
import no.nav.aap.fordeling.person.Diskresjonskode.ANY

data class EnhetsKriteria(
    val geografiskOmraade : String?,
    val skjermet : Boolean = false,
    val tema : String,
    val diskresjonskode : Diskresjonskode = ANY) {

    enum class Status {
        AKTIV,
        UNDER_ETABLERING,
        UNDER_AVVIKLING,
        NEDLAGT
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class NavOrg(val enhetNr : String, val status : String) {
        data class NAVEnhet(val enhetNr : String) {
            companion object {
                private const val FORDELING = "4303"
                val FORDELINGSENHET = NAVEnhet(FORDELING)
            }
        }
    }
}