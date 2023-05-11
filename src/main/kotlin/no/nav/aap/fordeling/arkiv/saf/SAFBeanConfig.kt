package no.nav.aap.fordeling.arkiv.saf

import com.fasterxml.jackson.databind.ObjectMapper
import java.util.*
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.graphql.client.HttpGraphQlClient
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClient.Builder
import no.nav.aap.api.felles.graphql.LoggingGraphQLInterceptor
import no.nav.aap.fordeling.arkiv.saf.SAFConfig.Companion.SAF
import no.nav.aap.fordeling.arkiv.saf.SAFConfig.Companion.SAFDOK
import no.nav.aap.fordeling.config.GlobalBeanConfig.Companion.clientCredentialFlow
import no.nav.aap.health.AbstractPingableHealthIndicator
import no.nav.boot.conditionals.ConditionalOnGCP
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenService
import no.nav.security.token.support.client.spring.ClientConfigurationProperties

@Configuration(proxyBeanMethods = false)
class SAFBeanConfig {

    @Bean
    @Qualifier(SAF)
    fun safGraphQLWebClient(builder : Builder, cfg : SAFConfig, @Qualifier(SAF) safFlow : ExchangeFilterFunction) =
        builder
            .baseUrl("${cfg.baseUri}/graphql")
            .filter(safFlow)
            .build()

    @Bean
    @Qualifier(SAFDOK)
    fun safWebClient(builder : Builder, cfg : SAFConfig, @Qualifier(SAF) safFlow : ExchangeFilterFunction) =
        builder
            .baseUrl("${cfg.baseUri}")
            .filter(safFlow)
            .build()

    @Bean
    @Qualifier(SAF)
    fun safGraphQLClient(@Qualifier(SAF) client : WebClient, mapper : ObjectMapper) =
        HttpGraphQlClient.builder(client)
            .interceptor(LoggingGraphQLInterceptor())
            .build()

    @Bean
    @Qualifier(SAF)
    fun safFlow(cfg : ClientConfigurationProperties, service : OAuth2AccessTokenService) = cfg.clientCredentialFlow(service, SAF)

    @Bean
    @ConditionalOnGCP
    fun safHealthIndicator(a : SAFGraphQLAdapter) = object : AbstractPingableHealthIndicator(a) {}
}