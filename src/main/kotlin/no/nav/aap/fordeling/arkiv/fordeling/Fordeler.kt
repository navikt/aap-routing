package no.nav.aap.fordeling.arkiv.fordeling

import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.fordeling.arkiv.fordeling.Fordeler.FordelingResultat.FordelingType.INGEN
import no.nav.aap.fordeling.navenhet.NAVEnhet

interface Fordeler {

    fun fordel(jp : Journalpost, enhet : NAVEnhet? = null) : FordelingResultat

    fun fordelManuelt(jp : Journalpost, enhet : NAVEnhet? = null) : FordelingResultat

    companion object {

        val FIKTIVTFNR = Fødselsnummer("19897599387")  // Fiktivt i tilfelle du lurte
    }

    data class FordelingResultat(val fordelingstype : FordelingType, val msg : String, val brevkode : String, val journalpostId : String = "0") {
        enum class FordelingType {
            AUTOMATISK,
            MANUELL_JOURNALFØRING,
            MANUELL_FORDELING,
            INGEN,
            ALLEREDE_OPPGAVE,
            ALLEREDE_JOURNALFØRT,
            INGEN_JOURNALPOST,
            DIREKTE_MANUELL,
            RACE
        }

        companion object {

            val INTET_RESULTAT = FordelingResultat(INGEN, "Ingen fordeling utført", "Ingen brevkode")
        }
    }
}

interface ManuellFordeler : Fordeler

class ManuellFordelingException(msg : String, cause : Throwable? = null) : RuntimeException(msg, cause)