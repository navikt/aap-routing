package no.nav.aap.fordeling.navenhet

import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Component
import no.nav.aap.fordeling.navenhet.NavEnhetConfig.Companion.NAVENHET
import no.nav.aap.fordeling.person.Diskresjonskode

@Component
class NavEnhetClient(private val a : NavEnhetWebClientAdapter) {

    fun navEnhet(område : String?, skjermet : Boolean, diskresjonskode : Diskresjonskode, tema : String) =
        a.navEnhet(EnhetsKriteria(område, skjermet, tema.uppercase(), diskresjonskode), a.aktiveEnheter())

    @Cacheable(NAVENHET)
    fun aktiveEnheter() = a.aktiveEnheter()

    fun erAktiv(enhet : NAVEnhet, aktiveEnheter : List<NAVEnhet>) : Boolean {
        return enhet in aktiveEnheter
    }
}