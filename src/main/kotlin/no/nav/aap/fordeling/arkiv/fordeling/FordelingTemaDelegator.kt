package no.nav.aap.fordeling.arkiv.fordeling

import no.nav.aap.fordeling.arkiv.fordeling.Fordeler.Companion.INGEN_FORDELER
import no.nav.aap.fordeling.navenhet.EnhetsKriteria.NAVEnhet
import no.nav.aap.fordeling.navenhet.EnhetsKriteria.NavOrg.NAVEnhet
import no.nav.aap.util.LoggerUtil
import org.springframework.stereotype.Component

@Component
class FordelingTemaDelegator(private val cfg: FordelingConfig, private val fordelere: List<Fordeler>) : Fordeler {

    val log = LoggerUtil.getLogger(FordelingTemaDelegator::class.java)

    init {
        log.info("Kan fordele følgende tema:\n ${fordelere.map { Pair(it.javaClass.simpleName, it.tema()) }}")
    }
    override fun tema() = fordelere.flatMap { it.tema() }
    override fun fordel(jp: Journalpost, enhet: NAVEnhet) = fordelerFor(jp.tema).fordel(jp,enhet)

    fun fordelerFor(tema: String) =
        if (cfg.enabled) {
            fordelere.first { tema.lowercase() in it.tema()}
        }
        else {
            INGEN_FORDELER.also {
                log.trace("Fordeling ikke aktivert, sett fordeling.enabled=true for å aktivere")
            }
        }
}