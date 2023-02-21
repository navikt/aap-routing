package no.nav.aap.fordeling.oppgave

import no.nav.aap.fordeling.egenansatt.EgenAnsattConfig.Companion.EGENANSATT
import no.nav.aap.fordeling.oppgave.OppgaveConfig.Companion.OPPGAVE
import no.nav.aap.health.AbstractPingableHealthIndicator
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
class OppgaveBeanConfig {

    @Qualifier(OPPGAVE)
    @Bean
    fun egenAnsattWebClient(builder: Builder, cfg: OppgaveConfig, @Qualifier(OPPGAVE) oppgaveClientCredentialFilterFunction: ExchangeFilterFunction) =
        builder
            .baseUrl("${cfg.baseUri}")
            .filter(oppgaveClientCredentialFilterFunction)
            .build()

    @Bean
    @Qualifier(OPPGAVE)
    fun oppgaveClientCredentialFilterFunction(cfgs: ClientConfigurationProperties, service: OAuth2AccessTokenService) =
        ExchangeFilterFunction { req, next ->
            next.exchange(ClientRequest.from(req).header(AUTHORIZATION, service.bearerToken(cfgs.registration[OPPGAVE], req.url())).build())
        }

    @Bean
    @ConditionalOnProperty("$OPPGAVE.enabled", havingValue = "true")
    fun oppgaveHealthIndicator(adapter: OppgaveWebClientAdapter) = object : AbstractPingableHealthIndicator(adapter) {}
}