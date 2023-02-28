package no.nav.aap.fordeling.arkiv

import com.fasterxml.jackson.databind.ObjectMapper
import graphql.kickstart.spring.webclient.boot.GraphQLWebClient
import no.nav.aap.fordeling.arkiv.ArkivConfig.Companion.DOKARKIV
import no.nav.aap.fordeling.arkiv.JournalpostDTO.JournalStatus.MOTTATT
import no.nav.aap.fordeling.config.GlobalBeanConfig.Companion.clientCredentialFlow
import no.nav.aap.health.AbstractPingableHealthIndicator
import no.nav.aap.util.Constants.JOARK
import no.nav.aap.util.LoggerUtil
import no.nav.aap.util.LoggerUtil.getLogger
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
import org.springframework.kafka.config.KafkaListenerEndpoint
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.listener.DefaultErrorHandler
import org.springframework.kafka.retrytopic.DestinationTopic.Properties
import org.springframework.kafka.retrytopic.RetryTopicNamesProviderFactory
import org.springframework.kafka.retrytopic.RetryTopicNamesProviderFactory.RetryTopicNamesProvider
import org.springframework.kafka.retrytopic.SuffixingRetryTopicNamesProviderFactory.SuffixingRetryTopicNamesProvider
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer.*
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.stereotype.Component
import org.springframework.util.backoff.FixedBackOff
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClient.Builder

@Configuration
@EnableScheduling
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
    @ConditionalOnProperty("${JOARK}.enabled", havingValue = "true")
    fun arkivHealthIndicator(adapter: ArkivWebClientAdapter) = object : AbstractPingableHealthIndicator(adapter) {}

    @Component
    class CustomRetryTopicNamesProviderFactory : RetryTopicNamesProviderFactory {

        val log = getLogger(javaClass)

        override fun createRetryTopicNamesProvider(properties: Properties): RetryTopicNamesProvider {
            return if (properties.isMainEndpoint) {
                log.info("IS MAIN")
                object : SuffixingRetryTopicNamesProvider(properties) {
                    override fun getTopicName(topic: String): String {
                        log.info("IS MAIN ${super.getTopicName(topic)}")
                        return "aap.routing-retry"
                    }
                }
            }
            else {
                object : SuffixingRetryTopicNamesProvider(properties) {
                    override fun getTopicName(topic: String): String {
                        log.info("NOT IS MAIN ${super.getTopicName(topic)}")
                        return "aap.routingdlt"
                    }
                }
            }
        }
    }
}