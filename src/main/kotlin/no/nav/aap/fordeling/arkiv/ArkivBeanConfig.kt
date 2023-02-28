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
import no.nav.joarkjournalfoeringhendelser.JournalfoeringHendelseRecord
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenService
import no.nav.security.token.support.client.spring.ClientConfigurationProperties
import org.apache.kafka.clients.producer.ProducerConfig.*
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.kafka.KafkaProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.listener.DefaultErrorHandler
import org.springframework.kafka.retrytopic.DestinationTopic.Properties
import org.springframework.kafka.retrytopic.RetryTopicConfiguration
import org.springframework.kafka.retrytopic.RetryTopicConfigurationBuilder
import org.springframework.kafka.retrytopic.RetryTopicNamesProviderFactory
import org.springframework.kafka.retrytopic.RetryTopicNamesProviderFactory.RetryTopicNamesProvider
import org.springframework.kafka.retrytopic.SuffixingRetryTopicNamesProviderFactory.SuffixingRetryTopicNamesProvider
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer.*
import org.springframework.retry.annotation.EnableRetry
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.stereotype.Component
import org.springframework.util.backoff.FixedBackOff
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClient.Builder

@Configuration
@EnableScheduling
@EnableRetry
class ArkivBeanConfig {

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
                setCommonErrorHandler(DefaultErrorHandler(FixedBackOff(1000L, 5L)).apply {
                    setRecordFilterStrategy {
                        with (it.value()) {
                            !(temaNytt.lowercase() in props.routing.keys && journalpostStatus == MOTTATT.name)
                        }
                    }
                })
            })
        }

    @Bean
    fun retryingKafkaOperations(p: KafkaProperties) =
        KafkaTemplate(DefaultKafkaProducerFactory<String, JournalfoeringHendelseRecord>(p.buildProducerProperties()))
    @Bean
    @ConditionalOnProperty("${JOARK}.enabled", havingValue = "true")
    fun arkivHealthIndicator(adapter: ArkivWebClientAdapter) = object : AbstractPingableHealthIndicator(adapter) {}

    @Component
    class TopicNamingProviderFactory : RetryTopicNamesProviderFactory {

        override fun createRetryTopicNamesProvider(p: Properties): RetryTopicNamesProvider {
            if (p.isMainEndpoint) {
                return SuffixingRetryTopicNamesProvider(p)
            }
            if (p.isDltTopic) {
                return object : SuffixingRetryTopicNamesProvider(p) {
                    override fun getTopicName(topic: String): String {
                        return "aap.routing.dlt"
                    }
                }
            }
            return object : SuffixingRetryTopicNamesProvider(p) {
                override fun getTopicName(topic: String): String {
                    return "aap.routing.retry"
                }
            }
        }
    }

    @Bean
    fun retryTopicConfig(props: KafkaProperties, factory: ConcurrentKafkaListenerContainerFactory<String, JournalfoeringHendelseRecord>, template: KafkaTemplate<String, JournalfoeringHendelseRecord>): RetryTopicConfiguration?{
        val cfg = props.retry.topic
        val backoff = FixedBackOff(cfg.delay.toMillis(), cfg.attempts.toLong())
        return RetryTopicConfigurationBuilder
            .newInstance()
           // .autoCreateTopics(false,2,2)
            .doNotAutoCreateRetryTopics()
            .fixedBackOff(backoff.interval)
            .maxAttempts(Math.toIntExact(backoff.maxAttempts))
            .useSingleTopicForFixedDelays()
           // .retryTopicSuffix(".retry")
            //.dltSuffix(".dlt")
            .doNotRetryOnDltFailure()
            .listenerFactory(factory)
            .create(template)
    }
}