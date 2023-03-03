package no.nav.aap.fordeling.arkiv

import com.fasterxml.jackson.databind.ObjectMapper
import graphql.kickstart.spring.webclient.boot.GraphQLWebClient
import io.confluent.kafka.serializers.KafkaAvroSerializer
import java.util.*
import no.nav.aap.fordeling.arkiv.ArkivConfig.Companion.DOKARKIV
import no.nav.aap.fordeling.arkiv.JournalpostDTO.JournalStatus.MOTTATT
import no.nav.aap.fordeling.config.GlobalBeanConfig.Companion.clientCredentialFlow
import no.nav.aap.health.AbstractPingableHealthIndicator
import no.nav.aap.util.Constants.JOARK
import no.nav.aap.util.LoggerUtil
import no.nav.joarkjournalfoeringhendelser.JournalfoeringHendelseRecord
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenService
import no.nav.security.token.support.client.spring.ClientConfigurationProperties
import org.apache.kafka.clients.producer.ProducerConfig.*
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.kafka.KafkaProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.retrytopic.RetryTopicComponentFactory
import org.springframework.kafka.retrytopic.RetryTopicConfigurationSupport
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer.*
import org.springframework.kafka.support.serializer.JsonSerializer
import org.springframework.retry.annotation.EnableRetry
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClient.Builder

@Configuration
@EnableScheduling
@EnableRetry
class ArkivBeanConfig : RetryTopicConfigurationSupport() {

    private val log = LoggerUtil.getLogger(ArkivBeanConfig::class.java)

    @Qualifier(JOARK)
    @Bean
    fun safGraphQLWebClient(builder: Builder, cfg: ArkivConfig, @Qualifier(JOARK) safFlow: ExchangeFilterFunction) =
        builder
            .baseUrl("${cfg.baseUri}")
            .filter(safFlow)
            .build()

    @Qualifier(DOKARKIV)
    @Bean
    fun dokarkivWebClient(builder: Builder, cfg: ArkivConfig, @Qualifier(DOKARKIV) dokarkivFlow: ExchangeFilterFunction) =
        builder
            .baseUrl("${cfg.dokarkiv}")
            .filter(dokarkivFlow)
            .build()

    @Qualifier(JOARK)
    @Bean
    fun safGraphQLClient(@Qualifier(JOARK) client: WebClient, mapper: ObjectMapper) = GraphQLWebClient.newInstance(client, mapper)

    @Bean
    @Qualifier(DOKARKIV)
    fun dokarkivFlow(cfg: ClientConfigurationProperties, service: OAuth2AccessTokenService) = cfg.clientCredentialFlow(service, DOKARKIV)

    @Bean
    @Qualifier(JOARK)
    fun safFlow(cfg: ClientConfigurationProperties, service: OAuth2AccessTokenService) = cfg.clientCredentialFlow(service, JOARK)
    @Bean(JOARK)
    fun arkivHendelserListenerContainerFactory(p: KafkaProperties,props: FordelerKonfig) =
        ConcurrentKafkaListenerContainerFactory<String, JournalfoeringHendelseRecord>().apply {
            consumerFactory = DefaultKafkaConsumerFactory(p.buildConsumerProperties().apply {
                setRecordFilterStrategy {
                    with (it.value()) {
                         !(temaNytt.lowercase() in props.routing.keys && journalpostStatus == MOTTATT.name)
                    }
                }
            })
        }

    @Bean
    fun defaultRetryTopicKafkaTemplate(p: KafkaProperties) =
        KafkaTemplate(DefaultKafkaProducerFactory<String, JournalfoeringHendelseRecord>(p.buildProducerProperties().apply {
            put(KEY_SERIALIZER_CLASS_CONFIG, StringSerializer::class.java)
            put(VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer::class.java)
        }))
    @Bean
    @ConditionalOnProperty("${JOARK}.enabled", havingValue = "true")
    fun arkivHealthIndicator(adapter: ArkivWebClientAdapter) = object : AbstractPingableHealthIndicator(adapter) {}

    override fun createComponentFactory() = object : RetryTopicComponentFactory() {
        override fun retryTopicNamesProviderFactory() = AAPNamespaceTopicNamingProviderFactory()
    }
}