package no.nav.aap.fordeling.arkiv

import jakarta.validation.constraints.NotEmpty
import no.nav.aap.fordeling.arkiv.Fordeler.Companion.INGEN_FORDELER
import no.nav.aap.fordeling.arkiv.FordelingConfiguration.Companion.FORDELING
import no.nav.aap.fordeling.arkiv.JournalpostDTO.JournalStatus
import no.nav.aap.util.LoggerUtil.getLogger
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(FORDELING)
data class FordelingConfiguration(val routing: @NotEmpty Map<String, FordelingProperties>) {
    val log = getLogger(javaClass)

    fun fordelerFor(jp: Journalpost, fordelere: List<Fordeler>) =
        routing[jp.tema]?.let { c ->
            if (jp.journalstatus in c.statuser && jp.dokumenter.any { it.brevkode in c.brevkoder }) {
                fordelere.firstOrNull { jp.tema in it.tema() }
            } else {
                INGEN_FORDELER.also {
                    log.info("Journalpost $jp med ${jp.tema} fordeles ikke")
                }
            }
        } ?: INGEN_FORDELER.also {
            log.info("Ingen konfigurasjon for ${jp.tema}")
        }


    data class FordelingProperties(val statuser: List<JournalStatus>, val brevkoder: List<String>)

    companion object {
        const val FORDELING = "fordeling"
    }
}