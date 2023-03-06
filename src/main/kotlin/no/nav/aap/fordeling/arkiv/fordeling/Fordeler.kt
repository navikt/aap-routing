package no.nav.aap.fordeling.arkiv.fordeling

import no.nav.aap.fordeling.arkiv.fordeling.Fordeler.FordelingResultat.Companion.NONE
import no.nav.aap.fordeling.arkiv.fordeling.Fordeler.FordelingResultat.FordelingType.INGEN
import no.nav.aap.fordeling.navenhet.EnhetsKriteria.NavOrg.NAVEnhet

interface Fordeler {

    fun tema(): List<String>

    fun fordel(jp: Journalpost, enhet: NAVEnhet) : FordelingResultat
    companion object {
        val INGEN_FORDELER =  object : Fordeler {
            override fun tema() = emptyList<String>()
            override fun fordel(jp: Journalpost, enhet: NAVEnhet) = NONE
        }
    }

    data class FordelingResultat(val journalpostId: String = "0", val msg: String, val type: FordelingType) {

         fun formattertMelding() = "${msg} for journalpost $journalpostId og fordelingstype $type"

        enum class FordelingType  {
            AUTOMATISK,MANUELL_JOURNALFØRING,MANUELL_FORDELING,INGEN
        }
        companion object {
            val NONE = FordelingResultat(msg ="Ingen fordeling utført", type =  INGEN)
        }
    }
}
interface ManuellFordeler: Fordeler