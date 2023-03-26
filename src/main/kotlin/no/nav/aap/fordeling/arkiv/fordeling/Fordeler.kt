package no.nav.aap.fordeling.arkiv.fordeling

import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.FordelingResultat
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.FordelingResultat.Companion.INGEN_FORDELING
import no.nav.aap.fordeling.navenhet.EnhetsKriteria.NavOrg.NAVEnhet
import no.nav.aap.util.Constants.AAP
import no.nav.boot.conditionals.Cluster
import no.nav.boot.conditionals.Cluster.Companion.devClusters
import no.nav.boot.conditionals.Cluster.Companion.prodClusters

interface Fordeler {

    val cfg: FordelerConfig
    fun fordel(jp: Journalpost, enhet: NAVEnhet? = null) : FordelingResultat
    fun fordelManuelt(jp: Journalpost, enhet: NAVEnhet? = null): FordelingResultat

    companion object {
        val INGEN_FORDELER = object : Fordeler {
            override val cfg =  FordelerConfig(emptyList(), emptyList())
            override fun fordel(jp: Journalpost, enhet: NAVEnhet?) = INGEN_FORDELING
            override fun fordelManuelt(jp: Journalpost, enhet: NAVEnhet?) = INGEN_FORDELING
        }
    }
}

data class FordelerConfig(val clusters: List<Cluster>, val tema: List<String>) {
    companion object {
        val DEV_AAP = of(devClusters(),AAP)
        val PROD_AAP = of(prodClusters(),AAP)
        private fun of( clusters: Array<Cluster>, vararg tema: String ) = FordelerConfig(clusters.toList(),tema.toList())
    }
}

interface ManuellFordeler : Fordeler

class ManuellFordelingException(msg: String, cause: Throwable? = null) : RuntimeException(msg, cause)