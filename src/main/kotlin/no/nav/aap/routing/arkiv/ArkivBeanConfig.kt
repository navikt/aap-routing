package no.nav.aap.routing.arkiv

import com.fasterxml.jackson.databind.ObjectMapper
import graphql.kickstart.spring.webclient.boot.GraphQLWebClient
import no.nav.aap.health.AbstractPingableHealthIndicator
import no.nav.aap.health.Pingable
import no.nav.aap.routing.arkiv.ArkivConfig.Companion.ARKIVHENDELSER
import no.nav.aap.routing.arkiv.ArkivConfig.Companion.CLIENT_CREDENTIALS_ARKIV
import no.nav.aap.util.Constants.AAP
import no.nav.aap.util.Constants.JOARK
import no.nav.aap.util.Constants.PDL_SYSTEM
import no.nav.aap.util.LoggerUtil
import no.nav.aap.util.TokenExtensions.bearerToken
import no.nav.joarkjournalfoeringhendelser.JournalfoeringHendelseRecord
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenService
import no.nav.security.token.support.client.spring.ClientConfigurationProperties
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.kafka.KafkaProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.core.KafkaAdmin
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer.*
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClient.Builder

@Configuration
class ArkivBeanConfig {
    private val log = LoggerUtil.getLogger(javaClass)

    @Qualifier(JOARK)
    @Bean
    fun webClientArkivSaf(builder: Builder, cfg: ArkivConfig, @Qualifier(JOARK) clientCredentialFilterFunction: ExchangeFilterFunction) =
        builder
            .baseUrl("${cfg.baseUri}")
            .filter(clientCredentialFilterFunction)
            .build()

    @Qualifier(JOARK)
    @Bean
    fun graphQLWebClientSaf(@Qualifier(JOARK) client: WebClient, mapper: ObjectMapper) = GraphQLWebClient.newInstance(client, mapper)

    @Bean
    @Qualifier(JOARK)
    fun clientCredentialFilterFunctionSaf(cfgs: ClientConfigurationProperties, service: OAuth2AccessTokenService) =
        ExchangeFilterFunction { req, next ->
            next.exchange(ClientRequest.from(req)
                .header(AUTHORIZATION, service.bearerToken(cfgs.registration[CLIENT_CREDENTIALS_ARKIV], req.url()))
                .build())
        }

    @Bean(ARKIVHENDELSER)
    fun arkivHendelserListenerContainerFactory(p: KafkaProperties) =
        ConcurrentKafkaListenerContainerFactory<String, JournalfoeringHendelseRecord>().apply {
            consumerFactory = DefaultKafkaConsumerFactory(p.buildConsumerProperties().apply {
               setRecordFilterStrategy { AAP != it.value().temaNytt.lowercase() }
            })
        }
}