package no.nav.aap.fordeling.arkiv.fordeling

import no.nav.aap.fordeling.arkiv.fordeling.Fordeler.Companion.INGEN_FORDELER
import no.nav.aap.util.LoggerUtil
import org.springframework.stereotype.Component

@Component
class FordelingFactory(private val cfg: FordelingConfig, private val fordelere: List<Fordeler>)  {

    val log = LoggerUtil.getLogger(FordelingFactory::class.java)

    init {
        log.info("Kan fordele følgende tema:\n${
            fordelere
                .filter { it !is ManuellFordeler }
                .map { Pair(it.javaClass.simpleName, it.tema()) }
                .map { "${it.second} -> ${it.first}" }
        }")
    }

    fun isEnabled() = cfg.isEnabled
    fun kanFordele(tema: String, status: String) = fordelerFor(tema) != INGEN_FORDELER

    fun fordelerFor(tema: String) =
            fordelere
                .filterNot { it is ManuellFordeler }
                .firstOrNull { tema.lowercase() in it.tema() } ?: INGEN_FORDELER
}