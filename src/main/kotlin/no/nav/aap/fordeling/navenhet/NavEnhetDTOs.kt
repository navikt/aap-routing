package no.nav.aap.fordeling.navenhet

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import no.nav.aap.fordeling.person.Diskresjonskode
import no.nav.aap.fordeling.person.Diskresjonskode.ANY

data class EnhetsKriteria(val geografiskOmraade : String?, val skjermet : Boolean = false, val tema : String, val diskresjonskode : Diskresjonskode = ANY)

@JsonIgnoreProperties(ignoreUnknown = true)
data class NAVEnhet(val enhetNr : String) {

    fun untatt() = enhetNr in UNTATTE_ENHETER

    companion object {

        private val UNTATTE_ENHETER = listOf("1891", "1893")
        const val FORDELINGKONTOR = "4303"
        const val AUTO_ENHET = "9999"
        val AUTOMATISK_JOURNALFØRING_ENHET = NAVEnhet(AUTO_ENHET)
        val FORDELINGSENHET = NAVEnhet(FORDELINGKONTOR)
    }
}