package no.nav.aap.fordeling.arkiv.fordeling

import no.nav.aap.fordeling.arkiv.fordeling.Fordeler.Companion.INGEN_FORDELER
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.JournalpostDTO.JournalStatus.MOTTATT
import no.nav.aap.fordeling.navenhet.EnhetsKriteria.NavOrg.NAVEnhet
import no.nav.aap.util.LoggerUtil
import org.springframework.stereotype.Component

@Component
class FordelingTemaDelegator(private val cfg: FordelingConfig, private val fordelere: List<Fordeler>) : Fordeler {

    val log = LoggerUtil.getLogger(FordelingTemaDelegator::class.java)

    init {
        log.info("Kan fordele følgende tema:\n${
            fordelere
                .filter { it !is ManuellFordeler }
                .map { Pair(it.javaClass.simpleName, it.tema()) }
                .map { "${it.second} -> ${it.first}" }
        }")
    }

    fun kanFordele(tema: String, status: String) = tema.lowercase() in tema() && status == MOTTATT.name
    override fun tema() = fordelere.flatMap { it.tema() }
    override fun fordel(jp: Journalpost, enhet: NAVEnhet) = fordelerFor(jp.tema).fordel(jp, enhet)

    fun fordelerFor(tema: String) =
        if (cfg.enabled) {
            fordelere
                .filterNot { it is ManuellFordeler }
                .first { tema.lowercase() in it.tema() }
        }
        else {
            INGEN_FORDELER
        }.also {
            log.info("Bruker fordeler ${it::class.java.simpleName} for tema $tema")
        }
}