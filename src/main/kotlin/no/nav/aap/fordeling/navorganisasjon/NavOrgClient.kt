package no.nav.aap.routing.navorganisasjon

import no.nav.aap.routing.person.Diskresjonskode
import org.springframework.stereotype.Component

@Component
class NavOrgClient(private val adapter: NavOrgWebClientAdapter) {
    fun navEnhet(område: String, skjermet: Boolean, kode: Diskresjonskode) =
        adapter.navEnhet(EnhetsKriteria(område, skjermet, kode),adapter.aktiveEnheter())
}