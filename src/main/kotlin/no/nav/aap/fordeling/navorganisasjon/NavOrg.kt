package no.nav.aap.fordeling.navorganisasjon

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import no.nav.aap.fordeling.navorganisasjon.EnhetsKriteria.Status

@JsonIgnoreProperties(ignoreUnknown = true)
data class NavOrg(val enhetNr: String, val status: String) {
    fun tilNavEnhet() = NavEnhet(enhetNr, Status.valueOf(status.uppercase().replace(' ','_')))
}
data class NavEnhet(val enhetNr: String, val status: Status)