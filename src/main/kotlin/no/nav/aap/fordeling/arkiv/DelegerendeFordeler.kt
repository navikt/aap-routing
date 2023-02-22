package no.nav.aap.fordeling.arkiv

import no.nav.aap.fordeling.navorganisasjon.NavEnhet
import org.springframework.stereotype.Component

@Component
class DelegerendeFordeler(private val cfg: FordelerKonfig, private val fordelere: List<Fordeler>) :
    Fordeler {
    override fun tema() = fordelere.flatMap { it.tema() }
    override fun fordel(jp: Journalpost, enhet: NavEnhet) = cfg.fordelerFor(jp,fordelere).fordel(jp,enhet)
}