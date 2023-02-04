package no.nav.aap.routing.navorganisasjon

import com.github.benmanes.caffeine.cache.Caffeine
import java.util.concurrent.TimeUnit.MINUTES
import no.nav.aap.health.AbstractPingableHealthIndicator
import no.nav.aap.routing.navorganisasjon.NavOrgConfig.Companion.NAVORG
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.caffeine.CaffeineCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer.*
import org.springframework.web.reactive.function.client.WebClient.Builder

@Configuration
@EnableCaching
class NavOrgBeanConfig {

    @Qualifier(NAVORG)
    @Bean
    fun navOrgWebClient(builder: Builder, cfg: NavOrgConfig) =
        builder
            .baseUrl("${cfg.baseUri}")
            .build()

    @Bean
    @ConditionalOnProperty("$NAVORG.enabled", havingValue = "true")
    fun orgHealthIndicator(adapter: NavOrgWebClientAdapter) = object : AbstractPingableHealthIndicator(adapter) {}
}