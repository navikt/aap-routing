package no.nav.aap.fordeling.arkiv.fordeling

import io.confluent.kafka.serializers.KafkaAvroSerializer
import java.util.*
import no.nav.aap.fordeling.arkiv.fordeling.FordelerKonfig.Companion.FORDELING
import no.nav.aap.fordeling.arkiv.fordeling.JournalpostDTO.JournalStatus.MOTTATT
import no.nav.aap.fordeling.config.AbstractKafkaHealthIndicator
import no.nav.aap.health.AbstractPingableHealthIndicator
import no.nav.joarkjournalfoeringhendelser.JournalfoeringHendelseRecord
import org.apache.kafka.clients.producer.ProducerConfig.*
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.kafka.KafkaProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaAdmin
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.listener.ContainerProperties.*
import org.springframework.kafka.listener.ContainerProperties.AckMode.*
import org.springframework.kafka.retrytopic.RetryTopicComponentFactory
import org.springframework.kafka.retrytopic.RetryTopicConfigurationSupport
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer.*
import org.springframework.retry.annotation.EnableRetry
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.stereotype.Component

@Configuration
@EnableScheduling
@EnableRetry
class FordelingBeanConfig(private val namingProviderFactory: AAPRetryTopicNamingProviderFactory) : RetryTopicConfigurationSupport() {

    @Component
    class FordelingPingable(admin: KafkaAdmin, p: KafkaProperties, cfg: FordelerKonfig) : AbstractKafkaHealthIndicator(admin,p.bootstrapServers,cfg)

    @Bean
    @ConditionalOnProperty("${FORDELING}.enabled", havingValue = "true")
    fun fordelerHealthIndicator(adapter: FordelingPingable) = object : AbstractPingableHealthIndicator(adapter) {}

     @Bean(FORDELING)
    fun fordelingListenerContainerFactory(p: KafkaProperties,props: FordelerKonfig) =
        ConcurrentKafkaListenerContainerFactory<String, JournalfoeringHendelseRecord>().apply {
            consumerFactory = DefaultKafkaConsumerFactory(p.buildConsumerProperties().apply {
                setRecordFilterStrategy {
                    with (it.value()) {
                         !(temaNytt.lowercase() in props.routing.keys && journalpostStatus == MOTTATT.name)
                    }
                }
            })
            containerProperties.ackMode = RECORD
        }

    @Bean
    fun defaultRetryTopicKafkaTemplate(p: KafkaProperties) =
        KafkaTemplate(DefaultKafkaProducerFactory<String, JournalfoeringHendelseRecord>(p.buildProducerProperties().apply {
            put(KEY_SERIALIZER_CLASS_CONFIG, StringSerializer::class.java)
            put(VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer::class.java)
        }))
    override fun createComponentFactory() = object : RetryTopicComponentFactory() {
        override fun retryTopicNamesProviderFactory() = namingProviderFactory
    }
}