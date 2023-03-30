package no.nav.aap.fordeling.arkiv.fordeling

import no.nav.aap.fordeling.arkiv.fordeling.Fordeler.Companion.INGEN_FORDELER
import no.nav.aap.fordeling.arkiv.fordeling.Fordeler.FordelerConfig
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.JournalpostDTO.JournalStatus.MOTTATT
import no.nav.aap.fordeling.navenhet.EnhetsKriteria.NavOrg.NAVEnhet
import no.nav.aap.util.Constants.AAP
import no.nav.aap.util.LoggerUtil
import no.nav.boot.conditionals.Cluster.Companion.currentCluster
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component

@Component
class FordelingFactory(private val fordelere : List<Fordeler>) : Fordeler, ApplicationListener<ApplicationReadyEvent> {

    private val log = LoggerUtil.getLogger(FordelingFactory::class.java)
    override val cfg = FordelerConfig(fordelere.flatMap { it.cfg.clusters }, listOf(AAP))

    override fun onApplicationEvent(event : ApplicationReadyEvent) = log.info("Kan fordele følgende tema:\n${
        fordelere
            .filter { it !is ManuellFordeler }
            .filter { currentCluster in it.cfg.clusters }
            .map { "${it.javaClass.simpleName} -> ${it.cfg.tema}" }
    }")

    fun kanFordele(tema : String, status : String) = fordelerFor(tema) != INGEN_FORDELER && status == MOTTATT.name

    private fun fordelerFor(tema : String) =
        (fordelere
            .filterNot { it is ManuellFordeler }
            .filter { currentCluster in it.cfg.clusters }
            .firstOrNull { tema.lowercase() in it.cfg.tema } ?: INGEN_FORDELER)

    override fun fordelManuelt(jp : Journalpost, enhet : NAVEnhet?) = fordelerFor(jp.tema).fordelManuelt(jp, enhet)

    override fun fordel(jp : Journalpost, enhet : NAVEnhet?) = fordelerFor(jp.tema).fordel(jp, enhet)

    override fun toString() = "FordelingFactory(fordelere=$fordelere)"
}