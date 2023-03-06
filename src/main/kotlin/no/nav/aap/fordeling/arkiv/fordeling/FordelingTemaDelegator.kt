package no.nav.aap.fordeling.arkiv.fordeling

import no.nav.aap.fordeling.arkiv.fordeling.Fordeler.Companion
import no.nav.aap.fordeling.arkiv.fordeling.Fordeler.Companion.INGEN_FORDELER
import no.nav.aap.fordeling.navorganisasjon.EnhetsKriteria.NAVEnhet
import no.nav.aap.util.LoggerUtil
import org.aspectj.weaver.tools.cache.SimpleCacheFactory.enabled
import org.springframework.stereotype.Component

@Component
class FordelingTemaDelegator(private val cfg: FordelingConfig, private val fordelere: List<Fordeler>) : Fordeler {

    val log = LoggerUtil.getLogger(FordelingTemaDelegator::class.java)

    init {
        log.info("Kan fordele følgende tema:\n ${fordelere.map { Pair(it.javaClass.simpleName, it.tema()) }}")
    }
    override fun tema() = fordelere.flatMap { it.tema() }
    override fun fordel(jp: Journalpost, enhet: NAVEnhet) = fordelerFor(jp,fordelere).fordel(jp,enhet)

    fun fordelerFor(jp: Journalpost, fordelere: List<Fordeler>) =
        if (cfg.enabled) {
            fordelere.first { jp.tema.lowercase() in it.tema()}
        }
        else {
            INGEN_FORDELER.also {
                log.trace("Fordeling ikke aktivert, sett fordeling.enabled=true for å aktivere")
            }
        }
}