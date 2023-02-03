package no.nav.aap.routing.arena

import no.nav.aap.health.AbstractPingableHealthIndicator
import no.nav.aap.routing.arena.ArenaConfig.Companion.ARENA
import no.nav.aap.routing.egenansatt.EgenAnsattConfig
import no.nav.aap.routing.egenansatt.EgenAnsattWebClientAdapter
import no.nav.aap.util.TokenExtensions.bearerToken
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenService
import no.nav.security.token.support.client.spring.ClientConfigurationProperties
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders.*
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer.*
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.WebClient.Builder

@Configuration
class ArenaBeanConfig {

    @Qualifier(ARENA)
    @Bean
    fun arenaWebClient(builder: Builder, cfg: EgenAnsattConfig, @Qualifier(ARENA) arenaClientCredentialFilterFunction: ExchangeFilterFunction) =
        builder
            .baseUrl("${cfg.baseUri}")
            .filter(arenaClientCredentialFilterFunction)
            .build()

    @Bean
    @Qualifier(ARENA)
    fun arenaClientCredentialFilterFunction(cfgs: ClientConfigurationProperties, service: OAuth2AccessTokenService) =
        ExchangeFilterFunction { req, next ->
            next.exchange(ClientRequest.from(req).header(AUTHORIZATION, service.bearerToken(cfgs.registration[ARENA], req.url())).build())
        }

    @Bean
    @ConditionalOnProperty("$ARENA.enabled", havingValue = "true")
    fun arenaHealthIndicator(adapter: EgenAnsattWebClientAdapter) = object : AbstractPingableHealthIndicator(adapter) {}
}