package no.nav.aap.fordeling.navenhet

import no.nav.aap.fordeling.navenhet.EnhetsKriteria.NavOrg
import no.nav.aap.fordeling.navenhet.NavEnhetConfig.Companion.NAVENHET
import no.nav.aap.fordeling.person.Diskresjonskode
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Component

@Component
class NavEnhetClient(private val a: NavEnhetWebClientAdapter) {
    fun navEnhet(område: String?, skjermet: Boolean, diskresjonskode: Diskresjonskode, tema: String) =
        a.navEnhet(EnhetsKriteria(område, skjermet, tema.uppercase(), diskresjonskode), a.aktiveEnheter())

    @Cacheable(NAVENHET)
    fun aktiveEnheter() = a.aktiveEnheter()
    fun erAktiv(enhetNr: String,aktiveEnheter: List<NavOrg>) = aktiveEnheter.any { it.enhetNr == enhetNr }
}