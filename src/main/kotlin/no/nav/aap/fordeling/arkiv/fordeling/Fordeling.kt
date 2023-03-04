package no.nav.aap.fordeling.arkiv.fordeling

import no.nav.aap.fordeling.arkiv.fordeling.Fordeling.FordelingResultat.Companion.NONE
import no.nav.aap.fordeling.arkiv.fordeling.Fordeling.FordelingResultat.FordelingType.INGEN
import no.nav.aap.fordeling.navorganisasjon.EnhetsKriteria.NAVEnhet

interface Fordeling {

    fun tema(): List<String>

    fun fordel(jp: Journalpost, enhet: NAVEnhet) : FordelingResultat
    companion object {
        val INGEN_FORDELER =  object : Fordeling {
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
            val NONE = FordelingResultat(msg ="Shit happens", type =  INGEN)
        }
    }
}
interface ManuellFordeling: Fordeling