package no.nav.aap.fordeling.arkiv.fordeling

import jakarta.validation.constraints.NotEmpty
import no.nav.aap.fordeling.arkiv.fordeling.Fordeler.Companion.INGEN_FORDELER
import no.nav.aap.fordeling.arkiv.fordeling.FordelingConfig.Companion.FORDELING
import no.nav.aap.fordeling.config.AbstractKafkaHealthIndicator.AbstractKafkaConfig
import no.nav.aap.util.LoggerUtil.getLogger
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.NestedConfigurationProperty
import org.springframework.boot.context.properties.bind.DefaultValue

@ConfigurationProperties(FORDELING)
data class FordelingConfig(@NestedConfigurationProperty val topics: FordelerTopics,
                           @DefaultValue("true") val enabled: Boolean) : AbstractKafkaConfig(FORDELING,enabled) {
    val log = getLogger(javaClass)

    fun fordelerFor(jp: Journalpost, fordelere: List<Fordeler>) =
        if (enabled) {
           fordelere.first { jp.tema.lowercase() in it.tema()}
        }
        else {
            INGEN_FORDELER.also {
                log.trace("Fordeling ikke aktivert, sett fordeling.enabled=true for å aktivere")
            }
        }

    data class FordelerTopics(val main: String,val retry: String = RETRY_TOPIC, val dlt: String = DLT_TOPIC, val backoff: Int, val retries: Int)

    companion object {
        const val RETRY_TOPIC = "aap.routing.retry"
        const val DLT_TOPIC = "aap.routing.dlt"
        const val FORDELING = "fordeling"
    }

    override fun topics(): List<String> =
        with(topics) {
            listOf(main,retry,dlt)
        }
}