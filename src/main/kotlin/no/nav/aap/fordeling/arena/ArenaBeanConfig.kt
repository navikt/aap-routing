package no.nav.aap.fordeling.arena

import no.nav.aap.fordeling.arena.ArenaConfig.Companion.ARENA
import no.nav.aap.health.AbstractPingableHealthIndicator
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders.*
import org.springframework.web.reactive.function.client.WebClient.Builder

@Configuration
class ArenaBeanConfig {

    @Qualifier(ARENA)
    @Bean
    fun arenaWebClient(builder: Builder, cfg: ArenaConfig) =
        builder
            .baseUrl("${cfg.baseUri}")
            .build()

    @Bean
    @ConditionalOnProperty("$ARENA.enabled", havingValue = "true")
    fun arenaHealthIndicator(adapter: ArenaWebClientAdapter) = object : AbstractPingableHealthIndicator(adapter) {}
}