package no.nav.aap.fordeling.arkiv.fordeling

import no.nav.aap.fordeling.navorganisasjon.EnhetsKriteria.NAVEnhet
import no.nav.aap.util.LoggerUtil
import org.springframework.stereotype.Component

@Component
class FordelingTemaDelegator(private val cfg: FordelingConfig, private val fordelere: List<Fordeler>) : Fordeler {

    val log = LoggerUtil.getLogger(FordelingTemaDelegator::class.java)

    override fun tema() = fordelere.flatMap { it.tema() }
    override fun fordel(jp: Journalpost, enhet: NAVEnhet) = cfg.fordelerFor(jp,fordelere).fordel(jp,enhet)

    init {
        log.info("Kan fordele følgende tema:\n ${fordelere.map { Pair(it.javaClass.simpleName, it.tema()) }}")
    }
}