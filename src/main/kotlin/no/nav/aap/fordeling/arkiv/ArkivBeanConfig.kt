package no.nav.aap.fordeling.arkiv

import com.fasterxml.jackson.databind.ObjectMapper
import graphql.kickstart.spring.webclient.boot.GraphQLWebClient
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.ANNOTATION_CLASS
import kotlin.annotation.AnnotationTarget.CLASS
import no.nav.aap.fordeling.arkiv.ArkivConfig.Companion.DOKARKIV
import no.nav.aap.fordeling.arkiv.JournalpostDTO.JournalStatus
import no.nav.aap.fordeling.arkiv.JournalpostDTO.JournalStatus.*
import no.nav.aap.fordeling.config.GlobalBeanConfig.Companion.clientCredentialFlow
import no.nav.aap.health.AbstractPingableHealthIndicator
import no.nav.aap.util.Constants.JOARK
import no.nav.joarkjournalfoeringhendelser.JournalfoeringHendelseRecord
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenService
import no.nav.security.token.support.client.spring.ClientConfigurationProperties
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.producer.ProducerConfig.*
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.kafka.KafkaProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.listener.DefaultErrorHandler
import org.springframework.kafka.listener.adapter.RecordFilterStrategy
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer.*
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.util.backoff.FixedBackOff
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClient.Builder



@Configuration
@EnableScheduling
class ArkivBeanConfig {

    @Qualifier(JOARK)
    @Bean
    fun arkivGraphQLWebClient(builder: Builder, cfg: ArkivConfig, @Qualifier(JOARK) arkivClientCredentialFlow: ExchangeFilterFunction) =
        builder
            .baseUrl("${cfg.baseUri}")
            .filter(arkivClientCredentialFlow)
            .build()

    @Qualifier(DOKARKIV)
    @Bean
    fun dokarkivWebClient(builder: Builder, cfg: ArkivConfig, @Qualifier(JOARK) arkivClientCredentialFlow: ExchangeFilterFunction) =
        builder
            .baseUrl("${cfg.dokarkiv}")
            .filter(arkivClientCredentialFlow)
            .build()

    @Qualifier(JOARK)
    @Bean
    fun arkivGraphQLClient(@Qualifier(JOARK) client: WebClient, mapper: ObjectMapper) = GraphQLWebClient.newInstance(client, mapper)

    @Bean
    @Qualifier(JOARK)
    fun arkivClientCredentialFlow(cfg: ClientConfigurationProperties, service: OAuth2AccessTokenService) = cfg.clientCredentialFlow(service, JOARK)
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
}