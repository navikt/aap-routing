package no.nav.aap.fordeling.arkiv.fordeling

import io.confluent.kafka.serializers.KafkaAvroSerializer
import java.util.*
import no.nav.aap.fordeling.arkiv.fordeling.FordelingConfig.Companion.FORDELING
import no.nav.aap.fordeling.config.KafkaPingable
import no.nav.aap.health.AbstractPingableHealthIndicator
import no.nav.boot.conditionals.ConditionalOnGCP
import no.nav.joarkjournalfoeringhendelser.JournalfoeringHendelseRecord
import org.apache.kafka.clients.producer.ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG
import org.apache.kafka.clients.producer.ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG
import org.apache.kafka.common.serialization.StringSerializer
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
class FordelingBeanConfig(private val namingProviderFactory: FordelingRetryTopicNamingProviderFactory) :
    RetryTopicConfigurationSupport() {

    @Component
    class FordelingPingable(admin: KafkaAdmin, p: KafkaProperties, cfg: FordelingConfig) :
        KafkaPingable(admin, p.bootstrapServers, cfg)

    @Bean
    @ConditionalOnGCP
    fun fordelerHealthIndicator(adapter: FordelingPingable) = object : AbstractPingableHealthIndicator(adapter) {}

    @Bean(FORDELING)
    fun fordelingListenerContainerFactory(p: KafkaProperties, fordeler: FordelingFactory) =
        ConcurrentKafkaListenerContainerFactory<String, JournalfoeringHendelseRecord>().apply {
            consumerFactory = DefaultKafkaConsumerFactory(p.buildConsumerProperties().apply {
                setRecordFilterStrategy {
                    with(it.value()) {
                        !(fordeler.kanFordele(temaNytt,journalpostStatus))
                    }
                }
            })
            containerProperties.ackMode = RECORD
        }

    @Bean
    fun defaultRetryTopicKafkaTemplate(p: KafkaProperties) =
        KafkaTemplate(DefaultKafkaProducerFactory<String, JournalfoeringHendelseRecord>(p.buildProducerProperties()
            .apply {
                put(KEY_SERIALIZER_CLASS_CONFIG, StringSerializer::class.java)
                put(VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer::class.java)
            }))

    override fun createComponentFactory() = object : RetryTopicComponentFactory() {
        override fun retryTopicNamesProviderFactory() = namingProviderFactory
    }
}