package no.nav.aap.fordeling.arkiv.fordeling

import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.FordelingResultat.Companion.INGEN_FORDELING
import no.nav.aap.fordeling.navenhet.EnhetsKriteria.NavOrg.NAVEnhet
import no.nav.boot.conditionals.Cluster

interface Fordeler {

    val cfg: FordelerConfig
    fun fordel(jp: Journalpost, enhet: NAVEnhet? = null) = INGEN_FORDELING
    fun fordelManuelt(jp: Journalpost, enhet: NAVEnhet? = null) = INGEN_FORDELING

    companion object {
        val INGEN_FORDELER = object : Fordeler {
            override val cfg =  FordelerConfig(emptyList(), emptyList())
        }
    }
}

data class FordelerConfig(val clusters: List<Cluster>, val tema: List<String>) {
    companion object {
        fun of( clusters: Array<Cluster>, vararg tema: String ) = FordelerConfig(clusters.toList(),tema.toList())
    }
}

interface ManuellFordeler : Fordeler

class ArenaSakException(msg: String) : RuntimeException(msg)
class ManuellFordelingException(msg: String, cause: Throwable? = null) : RuntimeException(msg, cause)