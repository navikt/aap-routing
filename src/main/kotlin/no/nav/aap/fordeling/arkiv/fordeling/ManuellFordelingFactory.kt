package no.nav.aap.fordeling.arkiv.fordeling

import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component
import no.nav.aap.fordeling.arkiv.fordeling.Fordeler.Companion.INGEN_FORDELER
import no.nav.aap.fordeling.arkiv.fordeling.Fordeler.FordelerConfig
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.FordelingResultat
import no.nav.aap.fordeling.navenhet.NAVEnhet
import no.nav.aap.util.Constants.AAP
import no.nav.aap.util.LoggerUtil
import no.nav.boot.conditionals.Cluster.Companion.currentCluster

@Component
class ManuellFordelingFactory(private val fordelere : List<ManuellFordeler>) : ManuellFordeler, ApplicationListener<ApplicationReadyEvent> {

    val log = LoggerUtil.getLogger(ManuellFordelingFactory::class.java)
    override val cfg = FordelerConfig(fordelere.flatMap { it.cfg.clusters }.toSet(), listOf(AAP))

    override fun onApplicationEvent(event : ApplicationReadyEvent) = log.info("Kan manuelt fordele følgende tema:\n${
        fordelere
            .filter { currentCluster in it.cfg.clusters }
            .map { "${it.javaClass.simpleName} -> ${it.cfg.tema}" }
    }")

    private fun fordelerFor(tema : String) =
        (fordelere
            .filter { currentCluster in it.cfg.clusters }
            .firstOrNull { tema.lowercase() in it.cfg.tema } ?: INGEN_FORDELER.also {
            log.warn("Ingen manuell fordeler for $tema i $currentCluster")
        })

    override fun fordel(jp : Journalpost, enhet : NAVEnhet?) : FordelingResultat {
        log.info("Fordeler journalpost ${jp.id} manuelt")
        return fordelerFor(jp.tema).fordel(jp, enhet)
    }

    override fun fordelManuelt(jp : Journalpost, enhet : NAVEnhet?) = fordel(jp, enhet)

    override fun toString() = "ManuellFordelingFactory(cfg=$cfg, fordelere=$fordelere)"
}