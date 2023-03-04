package no.nav.aap.fordeling.config

import no.nav.aap.health.Pingable
import no.nav.aap.util.LoggerUtil.getLogger
import org.springframework.kafka.core.KafkaAdmin

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

    private fun innslag(topic: String, ix: Int): Pair<String,String> {
        runCatching {
            with(admin.describeTopics(topic).values.first()) {
                return Pair("topic-$ix", "${name()} (${partitions().count()} partisjoner")
            }
        }.recover {
            return Pair(topic, it.message ?: it.javaClass.simpleName)
        }
        return Pair(topic,"OK")
    }

    abstract class AbstractKafkaConfig(val name: String, val isEnabled: Boolean) {
        abstract fun topics(): List<String>
    }
}