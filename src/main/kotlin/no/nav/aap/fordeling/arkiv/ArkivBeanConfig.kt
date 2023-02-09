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
import org.apache.kafka.clients.producer.ProducerConfig.*
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.serialization.StringSerializer
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
import org.springframework.kafka.listener.CommonErrorHandler
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer
import org.springframework.kafka.listener.DefaultErrorHandler
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer.*
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.util.backoff.FixedBackOff
import org.springframework.util.backoff.FixedBackOff.DEFAULT_INTERVAL
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClient.Builder

@Configuration
@EnableScheduling
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
    @Qualifier("dlt")
    fun dltOperations(p: KafkaProperties) =
        KafkaTemplate(DefaultKafkaProducerFactory<Any, Any>(p.buildProducerProperties()
            .apply {
                put(VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer::class.java)
                put(KEY_SERIALIZER_CLASS_CONFIG, StringSerializer::class.java)

            })).apply {
                defaultTopic = "aap.routingdlt"
        }
   @Bean
    fun deadLetterPublishingRecoverer(operations: KafkaOperations<Any,Any>) =
        DeadLetterPublishingRecoverer(operations) { r , _ -> TopicPartition("aap.routingdlt", 0) }

    @Bean
    fun errorHandler(recoverer: DeadLetterPublishingRecoverer) = DefaultErrorHandler(recoverer,FixedBackOff(DEFAULT_INTERVAL, 1L)).apply {
        setCommitRecovered(true)
    }
    @Bean(JOARK)
    fun arkivHendelserListenerContainerFactory(p: KafkaProperties, errorHandler: DefaultErrorHandler) =
        ConcurrentKafkaListenerContainerFactory<String, JournalfoeringHendelseRecord>().apply {
           setCommonErrorHandler(errorHandler)
            consumerFactory = DefaultKafkaConsumerFactory(p.buildConsumerProperties().apply {
                setRecordFilterStrategy { AAP != it.value().temaNytt.lowercase() }
               // setCommonErrorHandler(errorHandler)
            })
        }

    @Bean
    @ConditionalOnProperty("${JOARK}.enabled", havingValue = "true")
    fun arkivHealthIndicator(adapter: ArkivWebClientAdapter) = object : AbstractPingableHealthIndicator(adapter) {}
}