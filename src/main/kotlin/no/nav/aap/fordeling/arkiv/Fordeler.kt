package no.nav.aap.fordeling.arkiv

import no.nav.aap.fordeling.arkiv.Fordeler.FordelingResultat.Companion.NONE
import no.nav.aap.fordeling.navorganisasjon.NavEnhet

interface Fordeler {

    fun tema(): List<String>
    fun fordel(journalpost: Journalpost, enhet: NavEnhet) : FordelingResultat
    companion object {
        val INGEN_FORDELER =  object : Fordeler {
            override fun tema() = emptyList<String>()
            override fun fordel(journalpost: Journalpost, enhet: NavEnhet) = NONE
        }
    }

    data class FordelingResultat(val journalpostId: String = "0", val msg: String) {
        companion object {
            val NONE = FordelingResultat(msg ="Shit happens")
        }
    }
}
interface ManuellFordeler: Fordeler