package no.nav.aap.routing.skjerming

import no.nav.aap.health.AbstractPingableHealthIndicator
import no.nav.aap.routing.navorganisasjon.NavOrgConfig
import no.nav.aap.routing.navorganisasjon.NavOrgConfig.Companion.ORG
import no.nav.aap.routing.navorganisasjon.NavOrgWebClientAdapter
import no.nav.aap.routing.skjerming.SkjermingConfig.Companion.SKJERMING
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer.*
import org.springframework.web.reactive.function.client.WebClient.Builder

@Configuration
class SkjermingBeanConfig {

    @Qualifier(SKJERMING)
    @Bean
    fun skjermingWebClient(builder: Builder, cfg: SkjermingConfig) =
        builder
            .baseUrl("${cfg.baseUri}")
            .build()

    @Bean
    @ConditionalOnProperty("$SKJERMING.enabled", havingValue = "true")
    fun orgHealthIndicator(adapter: SkjermingWebClientAdapter) = object : AbstractPingableHealthIndicator(adapter) {}

}