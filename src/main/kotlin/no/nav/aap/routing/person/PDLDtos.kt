package no.nav.aap.routing.person

import no.nav.aap.routing.person.PDLGeoTilknytning.PDLGeoType.BYDEL
import no.nav.aap.routing.person.PDLGeoTilknytning.PDLGeoType.KOMMUNE
import no.nav.aap.routing.person.PDLGeoTilknytning.PDLGeoType.UTLAND

data class PDLGeoTilknytning(val gtType: PDLGeoType?, val gtKommune: String?, val gtBydel: String?, val gtLand: String?) {

   enum class PDLGeoType() {
       KOMMUNE,BYDEL,UTLAND,UDEFINERT
   }

   fun gt() = when(gtType){
       KOMMUNE -> gtKommune
       BYDEL -> gtBydel
       UTLAND -> gtLand
       else -> gtType?.name
   }
}