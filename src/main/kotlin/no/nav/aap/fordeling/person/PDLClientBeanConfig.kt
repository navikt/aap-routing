package no.nav.aap.fordeling.person

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.graphql.client.HttpGraphQlClient
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClient.Builder
import no.nav.aap.api.felles.graphql.LoggingGraphQLInterceptor
import no.nav.aap.fordeling.config.GlobalBeanConfig.Companion.clientCredentialFlow
import no.nav.aap.fordeling.person.PDLConfig.Companion.PDL
import no.nav.aap.health.AbstractPingableHealthIndicator
import no.nav.aap.rest.AbstractWebClientAdapter.Companion.behandlingFilterFunction
import no.nav.aap.rest.AbstractWebClientAdapter.Companion.temaFilterFunction
import no.nav.boot.conditionals.ConditionalOnGCP
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenService
import no.nav.security.token.support.client.spring.ClientConfigurationProperties

@Configuration(proxyBeanMethods = false)
class PDLClientBeanConfig {

    @Bean
    @Qualifier(PDL)
    fun pdlWebClient(b : Builder, cfg : PDLConfig, @Qualifier(PDL) pdlFlow : ExchangeFilterFunction) =
        b.baseUrl("${cfg.baseUri}")
            .filter(temaFilterFunction())
            .filter(behandlingFilterFunction())
            .filter(pdlFlow)
            .build()

    @Bean
    @Qualifier(PDL)
    fun pdlGraphQLClient(@Qualifier(PDL) client : WebClient) =
        HttpGraphQlClient.builder(client)
            .interceptor(LoggingGraphQLInterceptor())
            .build()

    @Bean
    @Qualifier(PDL)
    fun pdlClientFlow(cfg : ClientConfigurationProperties, service : OAuth2AccessTokenService) =
        cfg.clientCredentialFlow(service, PDL)

    @Bean
    @ConditionalOnGCP
    fun pdlHealthIndicator(a : PDLWebClientAdapter) = object : AbstractPingableHealthIndicator(a) {}
}