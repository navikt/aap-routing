package no.nav.aap.fordeling.arkiv

import org.springframework.stereotype.Component

@Component
class DelegerendeFordeler(private val cfg: FordelingConfiguration, private val fordelere: List<Fordeler>) :
    Fordeler {
    override fun tema() = fordelere.map(Fordeler::tema).flatten()
    override fun fordel(jp: Journalpost) =
        cfg.fordelerFor(jp,fordelere).fordel(jp)
}