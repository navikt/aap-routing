package no.nav.aap.fordeling.arkiv

import jakarta.validation.constraints.NotEmpty
import no.nav.aap.fordeling.arkiv.FordelingConfigurationProperties.Companion.FORDELING
import no.nav.aap.fordeling.arkiv.JournalpostDTO.JournalStatus
import no.nav.aap.util.LoggerUtil
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(FORDELING)
data class FordelingConfigurationProperties(val routing: @NotEmpty Map<String, FordelingProperties>) {

    fun finnFordeler(jp: Journalpost, tema: String, fordelere: List<Fordeler>):Fordeler?  {
        val log = LoggerUtil.getLogger(javaClass)
        return routing[tema.lowercase()]?.let { c ->
            if (jp.journalstatus in c.statuser && jp.dokumenter.any { it.brevkode in c.brevkoder }) {
                fordelere.find { it.tema().equals(tema, ignoreCase = true) }
            } else {
                log.info("Journalpost $jp for $tema rutes ikke")
                null
            }
        } ?: null
    }
    data class FordelingProperties(val statuser: List<JournalStatus>, val brevkoder: List<String>)

    companion object {
        const val FORDELING = "fordeling"
    }
}