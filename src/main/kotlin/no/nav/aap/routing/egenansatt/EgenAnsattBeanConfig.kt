package no.nav.aap.routing.egenansatt

import no.nav.aap.health.AbstractPingableHealthIndicator
import no.nav.aap.routing.egenansatt.EgenAnsattConfig.Companion.EGENANSATT
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer.*
import org.springframework.web.reactive.function.client.WebClient.Builder

@Configuration
class EgenAnsattBeanConfig {

    @Qualifier(EGENANSATT)
    @Bean
    fun egenAnsattWebClient(builder: Builder, cfg: EgenAnsattConfig) =
        builder
            .baseUrl("${cfg.baseUri}")
            .build()

    @Bean
    @ConditionalOnProperty("$EGENANSATT.enabled", havingValue = "true")
    fun egenAnsattHealthIndicator(adapter: EgenAnsattWebClientAdapter) = object : AbstractPingableHealthIndicator(adapter) {}
}