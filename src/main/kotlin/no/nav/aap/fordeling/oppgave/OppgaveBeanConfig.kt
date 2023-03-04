package no.nav.aap.fordeling.oppgave

import no.nav.aap.fordeling.config.GlobalBeanConfig.Companion.clientCredentialFlow
import no.nav.aap.fordeling.oppgave.OppgaveConfig.Companion.OPPGAVE
import no.nav.aap.health.AbstractPingableHealthIndicator
import no.nav.boot.conditionals.ConditionalOnGCP
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenService
import no.nav.security.token.support.client.spring.ClientConfigurationProperties
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer.*
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.WebClient.Builder

@Configuration
class OppgaveBeanConfig {

    @Qualifier(OPPGAVE)
    @Bean
    fun oppgaveWebClient(builder: Builder, cfg: OppgaveConfig, @Qualifier(OPPGAVE) oppgaveClientCredentialFlow: ExchangeFilterFunction) =
        builder
            .baseUrl("${cfg.baseUri}")
            .filter(oppgaveClientCredentialFlow)
            .build()

    @Bean
    @Qualifier(OPPGAVE)
    fun oppgaveClientCredentialFlow(cfg: ClientConfigurationProperties, service: OAuth2AccessTokenService) =
        cfg.clientCredentialFlow(service, OPPGAVE)

    @ConditionalOnGCP
    @Bean
    fun oppgaveHealthIndicator(adapter: OppgaveWebClientAdapter) = object : AbstractPingableHealthIndicator(adapter) {}
}