package no.nav.aap.fordeling.arkiv

import no.nav.aap.fordeling.arkiv.Fordeler.Companion
import no.nav.aap.fordeling.arkiv.Fordeler.FordelingResultat
import org.springframework.stereotype.Component

@Component
class DelegerendeFordeler(private val cfg: FordelerKonfig, private val fordelere: List<Fordeler>) :
    Fordeler {
    override fun tema() = fordelere.flatMap { it.tema() }
    override fun fordel(jp: Journalpost) = cfg.fordelerFor(jp,fordelere).fordel(jp)
}

@Component
class DelegerendeManuellFordeler(private val fordelere: List<ManuellFordeler>) : ManuellFordeler {
    override fun tema() = fordelere.map(Fordeler::tema).flatten()
    override fun fordel(journalpost: Journalpost): FordelingResultat = fordelere.find { journalpost.tema in it.tema() }?.let {
        it.fordel(journalpost)
    } ?: Fordeler.INGEN_FORDELER.fordel(journalpost)
}