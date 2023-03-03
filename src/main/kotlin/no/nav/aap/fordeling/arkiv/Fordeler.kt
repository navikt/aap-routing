package no.nav.aap.fordeling.arkiv

import no.nav.aap.fordeling.arkiv.Fordeler.FordelingResultat.Companion.NONE
import no.nav.aap.fordeling.arkiv.Fordeler.FordelingResultat.FordelingType.INGEN
import no.nav.aap.fordeling.navorganisasjon.EnhetsKriteria.NAVEnhet

interface Fordeler {

    fun tema(): List<String>
    fun fordel(journalpost: Journalpost, enhet: NAVEnhet) : FordelingResultat
    companion object {
        val INGEN_FORDELER =  object : Fordeler {
            override fun tema() = emptyList<String>()
            override fun fordel(journalpost: Journalpost, enhet: NAVEnhet) = NONE
        }
    }

    data class FordelingResultat(val journalpostId: String = "0", val msg: String, val type: FordelingType) {

         fun formattertMelding() = "${msg} for journalpost $journalpostId og fordelingstype $type"

        enum class FordelingType  {
            AUTOMATISK,MANUELL_JOURNALFØRING,MANUELL_FORDELING,INGEN
        }
        companion object {
            val NONE = FordelingResultat(msg ="Shit happens", type =  INGEN)
        }
    }
}
interface ManuellFordeler: Fordeler