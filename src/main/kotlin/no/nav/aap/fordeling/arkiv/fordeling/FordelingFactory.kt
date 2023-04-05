package no.nav.aap.fordeling.arkiv.fordeling

import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component
import no.nav.aap.fordeling.arkiv.fordeling.Fordeler.Companion.INGEN_FORDELER
import no.nav.aap.fordeling.arkiv.fordeling.Fordeler.FordelerConfig
import no.nav.aap.fordeling.navenhet.NAVEnhet
import no.nav.aap.util.Constants.AAP
import no.nav.aap.util.LoggerUtil
import no.nav.boot.conditionals.Cluster.Companion.currentCluster

@Component
class FordelingFactory(private val fordelere : List<Fordeler>) : Fordeler, ApplicationListener<ApplicationReadyEvent> {

    private val log = LoggerUtil.getLogger(FordelingFactory::class.java)
    override val cfg = FordelerConfig(fordelere.flatMap { it.cfg.clusters }.toSet(), listOf(AAP))

    override fun onApplicationEvent(event : ApplicationReadyEvent) = log.info("Kan fordele følgende tema:\n${
        fordelere
            .filter { it !is ManuellFordeler }
            .filter { currentCluster in it.cfg.clusters }
            .map { "${it.javaClass.simpleName} -> ${it.cfg.tema}" }
    }")

    override fun fordelManuelt(jp : Journalpost, enhet : NAVEnhet?) = fordelerFor(jp.tema).fordelManuelt(jp, enhet)

    override fun fordel(jp : Journalpost, enhet : NAVEnhet?) = fordelerFor(jp.tema).fordel(jp, enhet)

    private fun fordelerFor(tema : String) =
        (fordelere
            .filterNot { it is ManuellFordeler }
            .filter { currentCluster in it.cfg.clusters }
            .firstOrNull { tema.lowercase() in it.cfg.tema } ?: INGEN_FORDELER.also {
            log.trace("Ingen fordeler for {} i {}", tema, currentCluster)
        })

    override fun toString() = "FordelingFactory(fordelere=$fordelere)"
}