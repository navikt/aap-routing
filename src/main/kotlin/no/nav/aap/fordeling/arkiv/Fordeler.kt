package no.nav.aap.fordeling.arkiv

import no.nav.aap.fordeling.arkiv.Fordeler.FordelingResultat.Companion.NONE

interface Fordeler {
    fun tema(): List<String>
    fun fordel(journalpost: Journalpost) : FordelingResultat
    companion object {
        val INGEN_FORDELER =  object : Fordeler {
            override fun tema() = emptyList<String>()
            override fun fordel(journalpost: Journalpost) = NONE
        }
    }

    data class FordelingResultat(val status: String) {
        companion object {
            val NONE = FordelingResultat("Shit happens") // TODO
        }
    }
}
interface ManuellFordeler: Fordeler