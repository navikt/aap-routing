package no.nav.aap.fordeling.arkiv.fordeling

import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.FordelingResultat.Companion.INGEN_FORDELING
import no.nav.aap.fordeling.navenhet.EnhetsKriteria.NavOrg.NAVEnhet
import no.nav.boot.conditionals.Cluster
import no.nav.boot.conditionals.Cluster.Companion.devClusters
import no.nav.boot.conditionals.Cluster.Companion.prodClusters
import no.nav.boot.conditionals.Cluster.LOCAL
import no.nav.boot.conditionals.Cluster.TEST

interface Fordeler {

    fun clusters(): Array<Cluster>
    fun tema() = emptyList<String>()
    fun fordel(jp: Journalpost, enhet: NAVEnhet? = null) = INGEN_FORDELING
    fun fordelManuelt(jp: Journalpost, enhet: NAVEnhet? = null) = INGEN_FORDELING

    companion object {
        val INGEN_FORDELER = object : Fordeler {
            override fun clusters() = devClusters() + prodClusters() + LOCAL + TEST
        }
    }
}

interface ManuellFordeler : Fordeler

class ArenaSakException(msg: String) : RuntimeException(msg)
class ManuellFordelingException(msg: String, cause: Throwable? = null) : RuntimeException(msg, cause)