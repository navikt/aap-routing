package no.nav.aap.fordeling.arkiv.fordeling

import no.nav.aap.fordeling.arkiv.fordeling.Fordeler.FordelerConfig.Companion.of
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.FordelingResultat
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.FordelingResultat.Companion.INGEN_FORDELING
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

        val INGEN_FORDELER = object : Fordeler {
            override val cfg = of(emptyArray())
            override fun fordel(jp : Journalpost, enhet : NAVEnhet?) = INGEN_FORDELING
            override fun fordelManuelt(jp : Journalpost, enhet : NAVEnhet?) = INGEN_FORDELING
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