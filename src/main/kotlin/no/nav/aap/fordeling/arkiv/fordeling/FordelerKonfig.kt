package no.nav.aap.fordeling.arkiv.fordeling

import jakarta.validation.constraints.NotEmpty
import no.nav.aap.fordeling.arkiv.fordeling.Fordeler.Companion.INGEN_FORDELER
import no.nav.aap.fordeling.arkiv.fordeling.FordelerKonfig.Companion.FORDELING
import no.nav.aap.fordeling.config.AbstractKafkaHealthIndicator.AbstractKafkaConfig
import no.nav.aap.util.LoggerUtil.getLogger
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(FORDELING)
data class FordelerKonfig(val topics: FordelerTopics, val routing: @NotEmpty Map<String, FordelingProperties>, val enabled: Boolean = true) : AbstractKafkaConfig(
        FORDELING,true) {
    val log = getLogger(javaClass)

    fun fordelerFor(jp: Journalpost, fordelere: List<Fordeler>) =
        if (enabled) {
            routing[jp.tema]?.let {
                fordelere.firstOrNull { jp.tema in it.tema() }
            } ?: INGEN_FORDELER.also {
                log.info("Ingen konfigurasjon for tema ${jp.tema}")
            }
        }
        else {
            INGEN_FORDELER.also {
                log.info("Fordeling ikke aktivert, sett fordeling.enabled=true for å aktivere")
            }
        }

    data class FordelerTopics(val main: String,val retry: String, val dlt: String, val backoff: Int, val retries: Int)
    data class FordelingProperties(val brevkoder: List<String>)

    companion object {
        const val FORDELING = "fordeling"
    }

    override fun topics(): List<String> =
        with(topics) {
            listOf(main,retry,dlt)
        }
}