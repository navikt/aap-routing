package no.nav.aap.fordeling.arkiv.fordeling

import no.nav.aap.fordeling.arkiv.fordeling.Fordeler.Companion.INGEN_FORDELER
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.FordelingResultat
import no.nav.aap.fordeling.navenhet.EnhetsKriteria.NavOrg.NAVEnhet
import no.nav.aap.util.Constants.AAP
import no.nav.aap.util.LoggerUtil
import no.nav.boot.conditionals.Cluster.Companion.currentCluster
import org.springframework.stereotype.Component

@Component
class ManuellFordelingFactory(private val config: FordelingConfig, private val fordelere: List<ManuellFordeler>)  : ManuellFordeler {

    val log = LoggerUtil.getLogger(ManuellFordelingFactory::class.java)
    override val cfg = FordelerConfig(fordelere.flatMap { it.cfg.clusters }, listOf(AAP))

    init {
        log.info("Kan manuelt fordele følgende tema:\n${
            fordelere
                .filter {  currentCluster in it.cfg.clusters}
                .map { "${it.javaClass.simpleName} -> ${it.cfg.tema}" }
        }")
    }
    fun isEnabled() = config.isEnabled
    private fun fordelerFor(tema: String) =
        (fordelere
            .filter{currentCluster in it.cfg.clusters }
            .firstOrNull { tema.lowercase() in it.cfg.tema } ?: INGEN_FORDELER)

    override fun fordel(jp: Journalpost, enhet: NAVEnhet?) :FordelingResultat {
        log.info("Fordeler journalpost ${jp.journalpostId} manuelt")
        return fordelerFor(jp.tema).fordel(jp,enhet)
    }
    override fun fordelManuelt(jp: Journalpost, enhet: NAVEnhet?) = fordel(jp,enhet)

    override fun toString() = "ManuellFordelingFactory(cfg=$cfg, fordelere=$fordelere)"
}