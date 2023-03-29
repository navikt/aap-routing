package no.nav.aap.fordeling.arkiv.dokarkiv

import no.nav.aap.fordeling.arkiv.dokarkiv.DokarkivConfig.Companion.DOKARKIV
import no.nav.aap.fordeling.config.GlobalBeanConfig.Companion.clientCredentialFlow
import no.nav.aap.health.AbstractPingableHealthIndicator
import no.nav.boot.conditionals.ConditionalOnGCP
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenService
import no.nav.security.token.support.client.spring.ClientConfigurationProperties
import org.apache.kafka.clients.producer.ProducerConfig.*
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.listener.ContainerProperties.*
import org.springframework.kafka.listener.ContainerProperties.AckMode.*
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer.*
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.WebClient.Builder
import java.util.*

@Configuration
class DokarkivBeanConfig {

    @Bean
    @Qualifier(DOKARKIV)
    fun dokarkivWebClient(builder : Builder, cfg : DokarkivConfig, @Qualifier(DOKARKIV) dokarkivFlow : ExchangeFilterFunction) =
        builder
            .baseUrl("${cfg.baseUri}")
            .filter(dokarkivFlow)
            .build()

    @Bean
    @Qualifier(DOKARKIV)
    fun dokarkivFlow(cfg : ClientConfigurationProperties, service : OAuth2AccessTokenService) =
        cfg.clientCredentialFlow(service, DOKARKIV)

    @Bean
    @ConditionalOnGCP
    fun dokarkivHealthIndicator(adapter : DokarkivWebClientAdapter) =
        object : AbstractPingableHealthIndicator(adapter) {}
}