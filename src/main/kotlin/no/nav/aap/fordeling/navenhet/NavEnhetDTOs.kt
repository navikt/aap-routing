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
                private const val FORDELINGKONTOR = "4303"
                fun untatt(org : NavOrg) = org.enhetNr in UNTATTE_ENHETER
                const val AUTO_ENHET = "9999"
                private val UNTATTE_ENHETER = listOf("1891", "1893")
                val AUTOMATISK_JOURNALFØRING_ENHET = NAVEnhet(AUTO_ENHET)
                val FORDELINGSENHET = NAVEnhet(FORDELINGKONTOR)
            }
        }
    }
}