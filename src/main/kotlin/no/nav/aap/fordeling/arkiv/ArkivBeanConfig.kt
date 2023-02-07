package no.nav.aap.fordeling.arkiv

import com.fasterxml.jackson.databind.ObjectMapper
import graphql.kickstart.spring.webclient.boot.GraphQLWebClient
import io.confluent.kafka.serializers.KafkaAvroSerializer
import no.nav.aap.health.AbstractPingableHealthIndicator
import no.nav.aap.util.Constants.AAP
import no.nav.aap.util.Constants.JOARK
import no.nav.aap.util.TokenExtensions.bearerToken
import no.nav.joarkjournalfoeringhendelser.JournalfoeringHendelseRecord
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenService
import no.nav.security.token.support.client.spring.ClientConfigurationProperties
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerConfig.*
import org.apache.kafka.common.TopicPartition
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.kafka.KafkaProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaOperations
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer
import org.springframework.kafka.listener.DefaultErrorHandler
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer.*
import org.springframework.util.backoff.FixedBackOff
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClient.Builder

@Configuration
class ArkivBeanConfig {

    @Qualifier(JOARK)
    @Bean
    fun arkivWebClient(builder: Builder, cfg: ArkivConfig, @Qualifier(JOARK) clientCredentialFilterFunction: ExchangeFilterFunction) =
        builder
            .baseUrl("${cfg.baseUri}")
            .filter(clientCredentialFilterFunction)
            .build()

    @Qualifier(JOARK)
    @Bean
    fun arkivGraphQLClient(@Qualifier(JOARK) client: WebClient, mapper: ObjectMapper) = GraphQLWebClient.newInstance(client, mapper)

    @Bean
    @Qualifier(JOARK)
    fun safClientCredentialFilterFunction(cfgs: ClientConfigurationProperties, service: OAuth2AccessTokenService) =
        ExchangeFilterFunction { req, next ->
            next.exchange(ClientRequest.from(req)
                .header(AUTHORIZATION, service.bearerToken(cfgs.registration[JOARK], req.url()))
                .build())
        }

    @Bean
    fun dltOperations(p: KafkaProperties) =
        KafkaTemplate(DefaultKafkaProducerFactory<String, JournalfoeringHendelseRecord>(p.buildProducerProperties()
            .apply {
                put(VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer::class.java)
            }))
   @Bean
    fun deadLetterPublishingRecoverer(operations: KafkaOperations<String,JournalfoeringHendelseRecord>) =
        DeadLetterPublishingRecoverer(operations) { r , _ -> TopicPartition("aap.routing-dlt", r.partition()) }
    @Bean(JOARK)
    fun arkivHendelserListenerContainerFactory(p: KafkaProperties, recoverer: DeadLetterPublishingRecoverer) =
        ConcurrentKafkaListenerContainerFactory<String, JournalfoeringHendelseRecord>().apply {
            consumerFactory = DefaultKafkaConsumerFactory(p.buildConsumerProperties().apply {
                setRecordFilterStrategy { AAP != it.value().temaNytt.lowercase() }
                setCommonErrorHandler(DefaultErrorHandler(recoverer,FixedBackOff(0L, 1L)))
            })
        }

    @Bean
    @ConditionalOnProperty("${JOARK}.enabled", havingValue = "true")
    fun arkivHealthIndicator(adapter: ArkivWebClientAdapter) = object : AbstractPingableHealthIndicator(adapter) {}
}