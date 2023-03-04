package no.nav.aap.fordeling.arkiv.saf

import com.fasterxml.jackson.databind.ObjectMapper
import graphql.kickstart.spring.webclient.boot.GraphQLWebClient
import java.util.*
import no.nav.aap.fordeling.arkiv.saf.SafConfig.Companion.SAF
import no.nav.aap.fordeling.config.GlobalBeanConfig.Companion.clientCredentialFlow
import no.nav.aap.health.AbstractPingableHealthIndicator
import no.nav.boot.conditionals.ConditionalOnGCP
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenService
import no.nav.security.token.support.client.spring.ClientConfigurationProperties
import org.apache.kafka.clients.producer.ProducerConfig.*
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.listener.ContainerProperties.*
import org.springframework.kafka.listener.ContainerProperties.AckMode.*
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer.*
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClient.Builder

@Configuration
class SafBeanConfig {
    @Qualifier(SAF)
    @Bean
    fun safGraphQLWebClient(builder: Builder, cfg: SafConfig, @Qualifier(SAF) safFlow: ExchangeFilterFunction) =
        builder
            .baseUrl("${cfg.baseUri}")
            .filter(safFlow)
            .build()
    @Qualifier(SAF)
    @Bean
    fun safGraphQLClient(@Qualifier(SAF) client: WebClient, mapper: ObjectMapper) = GraphQLWebClient.newInstance(client, mapper)

    @Bean
    @Qualifier(SAF)
    fun safFlow(cfg: ClientConfigurationProperties, service: OAuth2AccessTokenService) = cfg.clientCredentialFlow(service, SAF)

    @ConditionalOnGCP
    @Bean
    fun safHealthIndicator(a: SafGraphQLAdapter) = object : AbstractPingableHealthIndicator(a) {}
}