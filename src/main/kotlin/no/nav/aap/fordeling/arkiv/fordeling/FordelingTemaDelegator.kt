package no.nav.aap.fordeling.arkiv.fordeling

import no.nav.aap.fordeling.arkiv.fordeling.Fordeler.Companion.INGEN_FORDELER
import no.nav.aap.fordeling.arkiv.fordeling.FordelingConfig.Companion.FORDELING
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.JournalpostDTO.JournalStatus.MOTTATT
import no.nav.aap.fordeling.config.Metrikker
import no.nav.aap.fordeling.config.Metrikker.Companion.BREVKODE
import no.nav.aap.fordeling.config.Metrikker.Companion.FORDELINGSTYPE
import no.nav.aap.fordeling.config.Metrikker.Companion.KANAL
import no.nav.aap.fordeling.navenhet.EnhetsKriteria.NavOrg.NAVEnhet
import no.nav.aap.util.Constants.TEMA
import no.nav.aap.util.LoggerUtil
import org.springframework.stereotype.Component

@Component
class FordelingTemaDelegator(private val cfg: FordelingConfig, private val fordelere: List<Fordeler>, private val metrikker: Metrikker) : Fordeler {

    val log = LoggerUtil.getLogger(FordelingTemaDelegator::class.java)

    init {
        log.info("Kan fordele følgende tema:\n${
            fordelere
                .filter { it !is ManuellFordeler }
                .map { Pair(it.javaClass.simpleName, it.tema()) }
                .map { "${it.second} -> ${it.first}" }
        }")
    }

    fun isEnabled() = cfg.isEnabled

    fun kanFordele(tema: String, status: String) = (tema.lowercase() in tema() && status == MOTTATT.name).also {
        log.trace("Kan fordele $tema $status er $it")
    }
    override fun tema() = fordelere.flatMap { it.tema() }
    override fun fordel(jp: Journalpost, enhet: NAVEnhet) = fordelerFor(jp.tema).fordel(jp, enhet).also {
        metrikker.inc(FORDELING, TEMA,jp.tema, FORDELINGSTYPE, it.fordelingstype.name, KANAL,jp.kanal, BREVKODE,it.brevkode)
    }

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