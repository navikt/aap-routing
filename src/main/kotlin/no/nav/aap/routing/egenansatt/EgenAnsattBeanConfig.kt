package no.nav.aap.routing.egenansatt

import no.nav.aap.health.AbstractPingableHealthIndicator
import no.nav.aap.routing.egenansatt.EgenAnsattConfig.Companion.EGENANSATT
import no.nav.aap.routing.person.PDLConfig
import no.nav.aap.routing.person.PDLConfig.Companion
import no.nav.aap.util.TokenExtensions.bearerToken
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenService
import no.nav.security.token.support.client.spring.ClientConfigurationProperties
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpHeaders.*
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer.*
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.WebClient.Builder

@Configuration
class EgenAnsattBeanConfig {

    @Qualifier(EGENANSATT)
    @Bean
    fun egenAnsattWebClient(builder: Builder, cfg: EgenAnsattConfig,@Qualifier(EGENANSATT) egenAnsattClientCredentialFilterFunction: ExchangeFilterFunction) =
        builder
            .baseUrl("${cfg.baseUri}")
            .filter(egenAnsattClientCredentialFilterFunction)
            .build()

    @Bean
    @Qualifier(EGENANSATT)
    fun egenAnsattClientCredentialFilterFunction(cfgs: ClientConfigurationProperties, service: OAuth2AccessTokenService) =
        ExchangeFilterFunction { req, next ->
            next.exchange(ClientRequest.from(req).header(AUTHORIZATION, service.bearerToken(cfgs.registration[EGENANSATT], req.url())).build())
        }

    @Bean
    @ConditionalOnProperty("$EGENANSATT.enabled", havingValue = "true")
    fun egenAnsattHealthIndicator(adapter: EgenAnsattWebClientAdapter) = object : AbstractPingableHealthIndicator(adapter) {}
}