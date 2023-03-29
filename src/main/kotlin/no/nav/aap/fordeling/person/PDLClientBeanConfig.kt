package no.nav.aap.fordeling.person

import com.fasterxml.jackson.databind.ObjectMapper
import graphql.kickstart.spring.webclient.boot.GraphQLWebClient
import no.nav.aap.fordeling.config.GlobalBeanConfig.Companion.clientCredentialFlow
import no.nav.aap.fordeling.person.PDLConfig.Companion.PDL
import no.nav.aap.health.AbstractPingableHealthIndicator
import no.nav.aap.rest.AbstractWebClientAdapter.Companion.temaFilterFunction
import no.nav.boot.conditionals.ConditionalOnGCP
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenService
import no.nav.security.token.support.client.spring.ClientConfigurationProperties
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClient.Builder

@Configuration
class PDLClientBeanConfig {
    @Bean
    @Qualifier(PDL)
    fun pdlWebClient(b : Builder, cfg : PDLConfig, @Qualifier(PDL) pdlFlow : ExchangeFilterFunction) =
        b.baseUrl("${cfg.baseUri}")
            .filter(temaFilterFunction())
            .filter(pdlFlow)
            .build()

    @Bean
    @Qualifier(PDL)
    fun pdlClientFlow(cfg : ClientConfigurationProperties, service : OAuth2AccessTokenService) =
        cfg.clientCredentialFlow(service, PDL)

    @Bean
    @Qualifier(PDL)
    fun pdlFlow(@Qualifier(PDL) client : WebClient, mapper : ObjectMapper) =
        GraphQLWebClient.newInstance(client, mapper)

    @Bean
    @ConditionalOnGCP
    fun pdlHealthIndicator(a : PDLWebClientAdapter) = object : AbstractPingableHealthIndicator(a) {}
}