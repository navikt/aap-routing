package no.nav.aap.fordeling.person

import no.nav.aap.fordeling.person.Diskresjonskode.ANY
import no.nav.aap.fordeling.person.Diskresjonskode.SPFO
import no.nav.aap.fordeling.person.Diskresjonskode.SPSF
import no.nav.aap.fordeling.person.PDLGeoTilknytning.PDLGeoType.BYDEL
import no.nav.aap.fordeling.person.PDLGeoTilknytning.PDLGeoType.KOMMUNE
import no.nav.aap.fordeling.person.PDLGeoTilknytning.PDLGeoType.UTLAND

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

data class PDLAdressebeskyttelse(val adressebeskyttelse: List<PDLGradering>)  {
    fun tilDiskresjonskode() = adressebeskyttelse.firstOrNull() ?.tilDiskresjonskode() ?: ANY
    data class PDLGradering(val gradering: PDLDiskresjonskode)  {
        fun tilDiskresjonskode() = gradering.tilDiskresjonskode() ?: ANY
        enum class PDLDiskresjonskode {
            FORTROLIG,STRENGT_FORTROLIG,STRENGT_FORTROLIG_UTLAND;

            fun tilDiskresjonskode() =
                when(this)  {
                    FORTROLIG -> SPFO
                    STRENGT_FORTROLIG,STRENGT_FORTROLIG_UTLAND -> SPSF
                }
        }
    }
}