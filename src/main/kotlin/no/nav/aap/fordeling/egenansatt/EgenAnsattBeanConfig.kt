package no.nav.aap.fordeling.egenansatt

import no.nav.aap.fordeling.config.GlobalBeanConfig.Companion.clientCredentialFlow
import no.nav.aap.fordeling.egenansatt.EgenAnsattConfig.Companion.EGENANSATT
import no.nav.aap.health.AbstractPingableHealthIndicator
import no.nav.boot.conditionals.ConditionalOnGCP
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenService
import no.nav.security.token.support.client.spring.ClientConfigurationProperties
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.WebClient.Builder

@Configuration(proxyBeanMethods = false)
class EgenAnsattBeanConfig {

    @Bean
    @Qualifier(EGENANSATT)
    fun egenAnsattWebClient(builder : Builder, cfg : EgenAnsattConfig, @Qualifier(EGENANSATT) egenAnsattFlow : ExchangeFilterFunction) =
        builder
            .baseUrl("${cfg.baseUri}")
            .filter(egenAnsattFlow)
            .build()

    @Bean
    @Qualifier(EGENANSATT)
    fun egenAnsattFlow(cfg : ClientConfigurationProperties, service : OAuth2AccessTokenService) =
        cfg.clientCredentialFlow(service, EGENANSATT)

    @Bean
    @ConditionalOnGCP
    fun egenAnsattHealthIndicator(adapter : EgenAnsattWebClientAdapter) =
        object : AbstractPingableHealthIndicator(adapter) {}
}