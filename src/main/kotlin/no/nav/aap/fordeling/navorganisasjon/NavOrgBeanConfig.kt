package no.nav.aap.fordeling.navorganisasjon

import com.github.benmanes.caffeine.cache.RemovalListener
import no.nav.aap.fordeling.navorganisasjon.NavOrgConfig.Companion.NAVORG
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
class NavOrgBeanConfig {

    private val log = LoggerUtil.getLogger(javaClass)


    @Bean
    @Qualifier(NAVORG)
    fun navOrgWebClient(builder: Builder, cfg: NavOrgConfig) =
        builder
            .baseUrl("${cfg.baseUri}")
            .build()

    @Bean
    fun cacheListener() =
        RemovalListener<Any, Any> { _, _, cause -> log.info("Cache removal $cause") }

    @Bean
    @ConditionalOnGCP
    fun orgHealthIndicator(adapter: NavOrgWebClientAdapter) = object : AbstractPingableHealthIndicator(adapter) {}
}