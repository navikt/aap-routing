package no.nav.aap.routing.navorganisasjon

import no.nav.aap.health.AbstractPingableHealthIndicator
import no.nav.aap.routing.navorganisasjon.NavOrgConfig.Companion.ORG
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer.*
import org.springframework.web.reactive.function.client.WebClient.Builder

@Configuration
class NavOrgBeanConfig {

    @Qualifier(ORG)
    @Bean
    fun navOrgWebClient(builder: Builder, cfg: NavOrgConfig) =
        builder
            .baseUrl("${cfg.baseUri}")
            .build()

    @Bean
    @ConditionalOnProperty("$ORG.enabled", havingValue = "true")
    fun orgHealthIndicator(adapter: NavOrgWebClientAdapter) = object : AbstractPingableHealthIndicator(adapter) {}

}