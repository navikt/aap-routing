package no.nav.aap.fordeling.oppgave

import no.nav.aap.fordeling.config.ChaosMonkeyConfig.Companion.monkeyIn
import no.nav.aap.fordeling.config.GlobalBeanConfig.Companion.clientCredentialFlow
import no.nav.aap.fordeling.oppgave.OppgaveConfig.Companion.OPPGAVE
import no.nav.aap.health.AbstractPingableHealthIndicator
import no.nav.aap.rest.AbstractWebClientAdapter.Companion.chaosMonkeyRequestFilterFunction
import no.nav.boot.conditionals.Cluster.Companion.notProdClusters
import no.nav.boot.conditionals.ConditionalOnGCP
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenService
import no.nav.security.token.support.client.spring.ClientConfigurationProperties
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.WebClient.Builder

@Configuration
class OppgaveBeanConfig {

    @Bean
    @Qualifier(OPPGAVE)
    fun oppgaveWebClient(builder: Builder, cfg: OppgaveConfig, @Qualifier(OPPGAVE) oppgaveFlow: ExchangeFilterFunction) =
        builder
            .baseUrl("${cfg.baseUri}")
            .filter(oppgaveFlow)
        //    .filter(chaosMonkeyRequestFilterFunction(monkeyIn(notProdClusters(),2)))
            .build()

    @Bean
    @Qualifier(OPPGAVE)
    fun oppgaveClientCredentialFlow(cfg: ClientConfigurationProperties, service: OAuth2AccessTokenService) =
        cfg.clientCredentialFlow(service, OPPGAVE)

    @Bean
    @ConditionalOnGCP
    fun oppgaveHealthIndicator(adapter: OppgaveWebClientAdapter) = object : AbstractPingableHealthIndicator(adapter) {}
}