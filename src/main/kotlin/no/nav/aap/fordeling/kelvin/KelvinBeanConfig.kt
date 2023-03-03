package no.nav.aap.fordeling.kelvin

import com.fasterxml.jackson.annotation.JsonInclude.Include.ALWAYS
import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.aap.fordeling.kelvin.AbstractKafkaHealthIndicator.AbstractKafkaConfig
import no.nav.aap.fordeling.kelvin.KelvinFordelingConfig.Companion.KELVIN
import no.nav.aap.health.AbstractPingableHealthIndicator
import no.nav.aap.health.Pingable
import no.nav.aap.util.LoggerUtil.getLogger
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.kafka.KafkaProperties
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.NestedConfigurationProperty
import org.springframework.boot.context.properties.bind.DefaultValue
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaAdmin
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.serializer.JsonSerializer
import org.springframework.stereotype.Component

@Configuration
class KelvinBeanConfig {

    @Component
    class KelvinPingable(admin: KafkaAdmin, p: KafkaProperties, cfg: KelvinFordelingConfig) : AbstractKafkaHealthIndicator(admin,p.bootstrapServers,cfg)

    @Bean
    @ConditionalOnProperty("$KELVIN.enabled", havingValue = "true")
    fun kelvinHealthIndicator(adapter: KelvinPingable) = object : AbstractPingableHealthIndicator(adapter) {}

    @Bean
    fun kelvinFordelingOperations(p: KafkaProperties, mapper: ObjectMapper) =
        KafkaTemplate(DefaultKafkaProducerFactory<String, Any>(p.buildProducerProperties()).apply {
            setValueSerializer(JsonSerializer(mapper.copy()
                .setDefaultPropertyInclusion(ALWAYS)))
        })
}
@ConfigurationProperties(KELVIN)
class KelvinFordelingConfig(
        @NestedConfigurationProperty val standard: KelvinTopicConfig,
        @NestedConfigurationProperty val ettersending: KelvinTopicConfig,
        @NestedConfigurationProperty val utland: KelvinTopicConfig,
        @DefaultValue("true") val enabled: Boolean) : AbstractKafkaConfig(KELVIN,enabled) {

    data class KelvinTopicConfig(val topic: String, @DefaultValue("true") val enabled: Boolean)

    companion object {
        const val KELVIN = "kelvin"
    }

    override fun topics() = listOf(standard.topic,ettersending.topic,utland.topic)

}

abstract class AbstractKafkaHealthIndicator(private val admin: KafkaAdmin,
                                            private val bootstrapServers: List<String>,
                                            private val cfg: AbstractKafkaConfig) : Pingable {
    override fun isEnabled() = cfg.isEnabled
    override fun pingEndpoint() = "$bootstrapServers"
    override fun name() = cfg.name

    val log = getLogger(javaClass)

    override fun ping(): Map<String, String> {
        log.info("Helsesjekker ${cfg.topics()}")
        return cfg.topics().mapIndexedNotNull { ix, topic -> innslag(topic,ix)}
            .associateBy({ it.first }, { it.second })
    }

    private fun innslag(topic: String, ix: Int): Pair<String,String>?{
        runCatching {
            with(admin.describeTopics(topic).values.first()) {
                return Pair("topic-$ix", "${name()} (${partitions().count()} partisjoner")
            }
        }.recover {
            return Pair(topic, it.message ?: it.javaClass.simpleName)
        }
        return null
    }

    abstract class AbstractKafkaConfig(val name: String, val isEnabled: Boolean) {
        abstract fun topics(): List<String>
    }
}