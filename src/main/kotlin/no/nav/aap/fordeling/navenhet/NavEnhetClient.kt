package no.nav.aap.fordeling.navenhet

import no.nav.aap.fordeling.person.Diskresjonskode
import org.springframework.stereotype.Component

@Component
class NavEnhetClient(private val a: NavEnhetWebClientAdapter) {
    fun navEnhet(område: String?, skjermet: Boolean, kode: Diskresjonskode, tema: String) =
        a.navEnhet(EnhetsKriteria(område, skjermet, kode, tema.uppercase()), a.aktiveEnheter())

    fun erAktiv(enhetNr: String) = a.aktiveEnheter().any { it.enhetNr == enhetNr }
}