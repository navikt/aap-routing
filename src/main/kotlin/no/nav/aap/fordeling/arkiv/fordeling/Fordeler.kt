package no.nav.aap.fordeling.arkiv.fordeling

import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.fordeling.arkiv.fordeling.Fordeler.FordelerConfig.Companion.of
import no.nav.aap.fordeling.arkiv.fordeling.Fordeler.FordelingResultat.Companion.INTET_RESULTAT
import no.nav.aap.fordeling.arkiv.fordeling.Fordeler.FordelingResultat.FordelingType.INGEN
import no.nav.aap.fordeling.navenhet.NAVEnhet
import no.nav.aap.util.Constants.AAP
import no.nav.boot.conditionals.Cluster
import no.nav.boot.conditionals.Cluster.Companion.devClusters
import no.nav.boot.conditionals.Cluster.Companion.prodClusters

interface Fordeler {

    val cfg : FordelerConfig

    fun fordel(jp : Journalpost, enhet : NAVEnhet? = null) : FordelingResultat

    fun fordelManuelt(jp : Journalpost, enhet : NAVEnhet? = null) : FordelingResultat

    companion object {

        val FIKTIVTFNR = Fødselsnummer("19897599387")  // Fiktivt i tilfelle du lurte

        val INGEN_FORDELER = object : Fordeler {
            override val cfg = of(emptyArray())
            override fun fordel(jp : Journalpost, enhet : NAVEnhet?) = INTET_RESULTAT
            override fun fordelManuelt(jp : Journalpost, enhet : NAVEnhet?) = INTET_RESULTAT
        }
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

    data class FordelerConfig(val clusters : Set<Cluster>, val tema : List<String>) {
        companion object {

            val LOCAL = of(arrayOf(Cluster.LOCAL), AAP)
            val DEV_AAP = of(devClusters(), AAP)
            val PROD_AAP = of(prodClusters(), AAP)
            fun of(clusters : Array<Cluster>, vararg tema : String) = FordelerConfig(clusters.toSet(), tema.toList())
        }
    }
}

interface ManuellFordeler : Fordeler

class ManuellFordelingException(msg : String, cause : Throwable? = null) : RuntimeException(msg, cause)