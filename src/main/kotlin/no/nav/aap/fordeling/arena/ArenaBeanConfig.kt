package no.nav.aap.fordeling.arena

import no.nav.aap.fordeling.arena.ArenaConfig.Companion.ARENA
import no.nav.aap.fordeling.config.GlobalBeanConfig.Companion.clientCredentialFlow
import no.nav.aap.health.AbstractPingableHealthIndicator
import no.nav.boot.conditionals.ConditionalOnGCP
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenService
import no.nav.security.token.support.client.spring.ClientConfigurationProperties
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders.*
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.WebClient.Builder

@Configuration(proxyBeanMethods = false)
class ArenaBeanConfig {

    @Bean
    @Qualifier(ARENA)
    fun arenaWebClient(builder : Builder, @Qualifier(ARENA) arenaFlow : ExchangeFilterFunction, cfg : ArenaConfig) =
        builder
            .baseUrl("${cfg.baseUri}")
            .filter(arenaFlow)
            .build()

    @Bean
    @Qualifier(ARENA)
    fun arenaFlow(cfg : ClientConfigurationProperties, service : OAuth2AccessTokenService) = cfg.clientCredentialFlow(service, ARENA)

    @Bean
    @ConditionalOnGCP
    fun arenaHealthIndicator(adapter : ArenaWebClientAdapter) = object : AbstractPingableHealthIndicator(adapter) {}
}