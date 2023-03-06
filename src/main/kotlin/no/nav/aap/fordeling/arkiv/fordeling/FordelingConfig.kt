package no.nav.aap.fordeling.arkiv.fordeling

import no.nav.aap.fordeling.arkiv.fordeling.FordelingConfig.Companion.FORDELING
import no.nav.aap.fordeling.config.AbstractKafkaConfig
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.NestedConfigurationProperty
import org.springframework.boot.context.properties.bind.DefaultValue

@ConfigurationProperties(FORDELING)
data class FordelingConfig(@NestedConfigurationProperty val topics: FordelingTopics,
                           @DefaultValue("true") val enabled: Boolean) : AbstractKafkaConfig(FORDELING,enabled) {

    data class FordelingTopics(@DefaultValue(DEFAULT_MAIN)  val main: String,
                               @DefaultValue(RETRY_TOPIC) val retry: String,
                               @DefaultValue(DLT_TOPIC) val dlt: String,
                               @DefaultValue(DEFAULT_BACKOFF) val backoff: Int,
                               @DefaultValue(DEFAULT_RETRIES)  val retries: Int)

    companion object {
        const val FORDELING = "fordeling"
        private const val DEFAULT_BACKOFF = "30000"
        private const val DEFAULT_RETRIES= "24"
        private const val RETRY_TOPIC = "aap.routing.retry"
        private const val DLT_TOPIC = "aap.routing.dlt"
        private const val DEFAULT_MAIN = "teamdokumenthandtering.aapen-dok-journalfoering"
    }

    override fun topics()  =
        with(topics) {
            listOf(main,retry,dlt)
        }
}