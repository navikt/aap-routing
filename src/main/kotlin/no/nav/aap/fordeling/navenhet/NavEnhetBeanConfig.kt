package no.nav.aap.fordeling.navenhet

import com.github.benmanes.caffeine.cache.RemovalListener
import no.nav.aap.fordeling.navenhet.NavEnhetConfig.Companion.NAVENHET
import no.nav.aap.health.AbstractPingableHealthIndicator
import no.nav.aap.util.LoggerUtil
import no.nav.boot.conditionals.ConditionalOnGCP
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer.*
import org.springframework.web.reactive.function.client.WebClient.Builder

@Configuration
@EnableCaching
class NavEnhetBeanConfig {

    private val log = LoggerUtil.getLogger(javaClass)


    @Bean
    @Qualifier(NAVENHET)
    fun navEnhetWebClient(builder: Builder, cfg: NavEnhetConfig) =
        builder
            .baseUrl("${cfg.baseUri}")
            .build()

    @Bean
    fun cacheListener() =
        RemovalListener<Any, Any> { _, _, cause -> log.info("Cache removal $cause") }

    @Bean
    @ConditionalOnGCP
    fun navEnhetHealthIndicator(adapter: NavEnhetWebClientAdapter) = object : AbstractPingableHealthIndicator(adapter) {}
}