package no.nav.aap.fordeling.fordeling

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.NestedConfigurationProperty
import no.nav.aap.fordeling.fordeling.FordelingConfig.Companion.FORDELING
import no.nav.aap.fordeling.config.KafkaConfig

@ConfigurationProperties(FORDELING)
data class FordelingConfig(@NestedConfigurationProperty val topics : FordelingTopics = FordelingTopics(),
                           val enabled : Boolean = true) : KafkaConfig(FORDELING, enabled) {

    override fun topics() = topics.all

    data class FordelingTopics(val main : String = DEFAULT_MAIN, val retry : String = RETRY_TOPIC,
                               val dlt : String = DLT_TOPIC, val backoff : Int = DEFAULT_BACKOFF, val retries : Int = DEFAULT_RETRIES
    ) {

        val all = listOf(main, retry, dlt)
    }

    companion object {

        const val FORDELING = "fordeling"
        private const val DEFAULT_BACKOFF = 30000
        private const val DEFAULT_RETRIES = 24
        private const val RETRY_TOPIC = "aap.routing.retry"
        private const val DLT_TOPIC = "aap.routing.dlt"
        private const val DEFAULT_MAIN = "teamdokumenthandtering.aapen-dok-journalfoering"
    }
}