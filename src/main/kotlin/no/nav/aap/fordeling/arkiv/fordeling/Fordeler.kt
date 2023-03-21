package no.nav.aap.fordeling.arkiv.fordeling

import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.FordelingResultat.Companion.INGEN_FORDELING
import no.nav.aap.fordeling.navenhet.EnhetsKriteria.NavOrg.NAVEnhet

interface Fordeler {
    fun tema() = emptyList<String>()
    fun fordel(jp: Journalpost, enhet: NAVEnhet) = INGEN_FORDELING
    fun fordelManuelt(jp: Journalpost, enhet: NAVEnhet) = INGEN_FORDELING

    companion object {
        val INGEN_FORDELER = object : Fordeler {}
    }
}

interface ManuellFordeler : Fordeler