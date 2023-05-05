package no.nav.aap.fordeling.person

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue
import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.fordeling.person.Diskresjonskode.ANY
import no.nav.aap.fordeling.person.Diskresjonskode.SPFO
import no.nav.aap.fordeling.person.Diskresjonskode.SPSF
import no.nav.aap.fordeling.person.PDLGeoTilknytning.PDLGeoType.BYDEL
import no.nav.aap.fordeling.person.PDLGeoTilknytning.PDLGeoType.KOMMUNE
import no.nav.aap.fordeling.person.PDLGeoTilknytning.PDLGeoType.UTLAND

data class Identer(val identer : List<Ident>) {
    data class Ident(val ident : Fødselsnummer)

    fun fnr() = identer.first().ident
}

data class PDLGeoTilknytning(val gtType : PDLGeoType, val gtKommune : String?, val gtBydel : String?, val gtLand : String?) {

    enum class PDLGeoType { KOMMUNE, BYDEL, UTLAND,

        @JsonEnumDefaultValue
        UDEFINERT
    }

    fun gt() = when (gtType) {
        KOMMUNE -> gtKommune
        BYDEL -> gtBydel
        UTLAND -> gtLand
        else -> gtType.name
    }
}

enum class Diskresjonskode { SPFO, SPSF, ANY }

data class PDLAdressebeskyttelse(val adressebeskyttelse : List<PDLGradering> = emptyList()) {

    fun tilDiskresjonskode() = adressebeskyttelse.firstOrNull()?.tilDiskresjonskode() ?: ANY

    data class PDLGradering(val gradering : PDLDiskresjonskode) {

        fun tilDiskresjonskode() = gradering.tilDiskresjonskode()
        enum class PDLDiskresjonskode {
            FORTROLIG, STRENGT_FORTROLIG, STRENGT_FORTROLIG_UTLAND;

            fun tilDiskresjonskode() =
                when (this) {
                    FORTROLIG -> SPFO
                    STRENGT_FORTROLIG, STRENGT_FORTROLIG_UTLAND -> SPSF
                }
        }
    }
}