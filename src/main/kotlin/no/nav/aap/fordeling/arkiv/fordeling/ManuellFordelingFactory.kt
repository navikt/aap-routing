package no.nav.aap.fordeling.arkiv.fordeling

import no.nav.aap.fordeling.arkiv.fordeling.Fordeler.Companion.INGEN_FORDELER
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.FordelingResultat
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.JournalpostDTO.JournalStatus.MOTTATT
import no.nav.aap.fordeling.navenhet.EnhetsKriteria.NavOrg.NAVEnhet
import no.nav.aap.util.LoggerUtil
import no.nav.boot.conditionals.Cluster
import no.nav.boot.conditionals.Cluster.Companion.currentCluster
import org.springframework.stereotype.Component

@Component
class ManuellFordelingFactory(private val cfg: FordelingConfig, private val fordelere: List<ManuellFordeler>)   {

    val log = LoggerUtil.getLogger(ManuellFordelingFactory::class.java)

    init {
        log.info("Kan manuelt fordele følgende tema:\n${
            fordelere
                .filter {  currentCluster in it.clusters()}
                .map { Pair(it.javaClass.simpleName, it.tema()) }
                .map { "${it.second} -> ${it.first}" }
        }")
    }
    fun isEnabled() = cfg.isEnabled
    fun fordelerFor(tema: String) : Fordeler{
        log.info("Finner fordeler for $tema blant $fordelere")
       return  (fordelere
                .filter{currentCluster in it.clusters() }
                .firstOrNull { tema.lowercase() in it.tema() } ?: INGEN_FORDELER).also {
                    log.info("Manuell fordeler er $it")
        }
    }

    override fun toString() = "ManuellFordelingFactory(cfg=$cfg, fordelere=$fordelere)"
}