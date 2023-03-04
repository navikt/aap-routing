package no.nav.aap.fordeling.arkiv.fordeling

import no.nav.aap.fordeling.navorganisasjon.EnhetsKriteria.NAVEnhet
import org.springframework.stereotype.Component

@Component
class FordelingTemaDelegator(private val cfg: FordelingConfig, private val fordelere: List<Fordeling>) : Fordeling {
    override fun tema() = fordelere.flatMap { it.tema() }
    override fun fordel(jp: Journalpost, enhet: NAVEnhet) = cfg.fordelerFor(jp,fordelere).fordel(jp,enhet)
}