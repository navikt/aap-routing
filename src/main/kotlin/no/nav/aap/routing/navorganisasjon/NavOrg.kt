package no.nav.aap.routing.navorganisasjon

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import no.nav.aap.routing.navorganisasjon.EnhetsKriteria.Status
import no.nav.aap.routing.navorganisasjon.EnhetsKriteria.Status.*

@JsonIgnoreProperties(ignoreUnknown = true)
data class NavOrg(val enhetNr: String, val status: String) {
    fun tilNavEnhet() =
        when (status)  {
            "Aktiv" -> NavEnhet(enhetNr,AKTIV)
            "Under etablering" -> NavEnhet(enhetNr,UNDER_ETABLERING)
            "Under avvikling" -> NavEnhet(enhetNr,UNDER_AVVIKLING)
            "Nedlagt" -> NavEnhet(enhetNr,NEDLAGT)
            else -> throw IllegalArgumentException("Status $status fra Norg2 er ukjent")
        }
}
data class NavEnhet(val enhetNr: String, val status: Status)