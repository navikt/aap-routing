package no.nav.aap.fordeling.arkiv

import no.nav.aap.fordeling.arkiv.Fordeler.Companion
import no.nav.aap.fordeling.arkiv.Fordeler.Companion.INGEN_FORDELER
import no.nav.aap.fordeling.arkiv.Fordeler.FordelingResultat
import org.springframework.stereotype.Component

@Component
class DelegerendeManuellFordeler(private val fordelere: List<ManuellFordeler>) : ManuellFordeler {
    override fun tema() = fordelere.map(Fordeler::tema).flatten()
    override fun fordel(journalpost: Journalpost): FordelingResultat = fordelere.find { journalpost.tema in it.tema() }?.let {
        it.fordel(journalpost)
    } ?: INGEN_FORDELER.fordel(journalpost)
}