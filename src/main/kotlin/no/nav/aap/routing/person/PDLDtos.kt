package no.nav.aap.routing.person

import no.nav.aap.routing.person.Diskresjonskode.ANY
import no.nav.aap.routing.person.Diskresjonskode.SPFO
import no.nav.aap.routing.person.Diskresjonskode.SPSF
import no.nav.aap.routing.person.PDLGeoTilknytning.PDLGeoType.BYDEL
import no.nav.aap.routing.person.PDLGeoTilknytning.PDLGeoType.KOMMUNE
import no.nav.aap.routing.person.PDLGeoTilknytning.PDLGeoType.UTLAND

data class PDLGeoTilknytning(val gtType: PDLGeoType?, val gtKommune: String?, val gtBydel: String?, val gtLand: String?) {

   enum class PDLGeoType() {
       KOMMUNE,BYDEL,UTLAND,UDEFINERT
   }

    fun gt() = when(gtType) {
        KOMMUNE -> gtKommune
        BYDEL -> gtBydel
        UTLAND -> gtLand
        else -> gtType?.name
    }
}

enum class Diskresjonskode { SPFO, SPSF, ANY}

data class PDLDiskresjonskoder(val hentPerson: PDLKoder) {
    fun tilDiskresjonskode() = hentPerson.gradering.firstOrNull()?.tilDiskresjonskode() ?: ANY
}

data class PDLKoder(val gradering: List<PDLDiskresjonskode>)


enum class PDLDiskresjonskode() {
    FORTROLIG,STRENGT_FORTROLIG,STRENGT_FORTROLIG_UTLAND;

    fun tilDiskresjonskode() =
        when(this)  {
            FORTROLIG -> SPFO
            STRENGT_FORTROLIG,STRENGT_FORTROLIG_UTLAND -> SPSF
        }
}