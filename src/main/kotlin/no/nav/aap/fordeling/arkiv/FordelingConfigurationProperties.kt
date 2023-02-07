package no.nav.aap.fordeling.arkiv

import jakarta.validation.constraints.NotEmpty
import no.nav.aap.fordeling.arkiv.FordelingConfigurationProperties.Companion.FORDELING
import no.nav.aap.fordeling.arkiv.JournalpostDTO.JournalStatus
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(FORDELING)
data class FordelingConfigurationProperties(val routing: @NotEmpty Map<String, FordelingProperties>) {
    data class FordelingProperties(val statuser: List<JournalStatus>, val brevkoder: List<String>)

    companion object {
        const val FORDELING = "fordeling"
    }
}