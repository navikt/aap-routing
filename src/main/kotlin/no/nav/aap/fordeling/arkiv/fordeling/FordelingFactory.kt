package no.nav.aap.fordeling.arkiv.fordeling

import no.nav.aap.fordeling.arkiv.fordeling.Fordeler.Companion.INGEN_FORDELER
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.JournalpostDTO.JournalStatus.MOTTATT
import no.nav.aap.util.LoggerUtil
import no.nav.boot.conditionals.Cluster.Companion.currentCluster
import org.springframework.stereotype.Component

@Component
class FordelingFactory(private val cfg: FordelingConfig, private val fordelere: List<Fordeler>)  {

    val log = LoggerUtil.getLogger(FordelingFactory::class.java)

    init {
        log.info("Kan fordele følgende tema:\n${
            fordelere
                .filter { it !is ManuellFordeler }
                .filter{currentCluster in it.clusters() }
                .map { Pair(it.javaClass.simpleName, it.tema()) }
                .map { "${it.second} -> ${it.first}" }
        }")
    }

    fun isEnabled() = cfg.isEnabled
    fun kanFordele(tema: String, status: String) = fordelerFor(tema) != INGEN_FORDELER && status == MOTTATT.name

    fun fordelerFor(tema: String) =
        (fordelere
            .filterNot { it is ManuellFordeler }
            .filter{currentCluster in it.clusters() }
            .firstOrNull { tema.lowercase() in it.tema() } ?: INGEN_FORDELER).also {
            log.info("Fordeler er $this")
        }

    override fun toString() = "FordelingFactory(fordelere=$fordelere)"
}