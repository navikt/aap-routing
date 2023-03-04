package no.nav.aap.fordeling.arkiv.fordeling

import no.nav.aap.fordeling.navorganisasjon.EnhetsKriteria.NAVEnhet
import org.springframework.stereotype.Component

@Component
class DelegerendeFordeler(private val cfg: FordelerKonfig, private val fordelere: List<Fordeler>) : Fordeler {
    override fun tema() = fordelere.flatMap { it.tema() }
    override fun fordel(jp: Journalpost, enhet: NAVEnhet) = cfg.fordelerFor(jp,fordelere).fordel(jp,enhet)
}