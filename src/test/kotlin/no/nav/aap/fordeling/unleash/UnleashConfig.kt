package no.nav.aap.fordeling.unleash

import io.getunleash.DefaultUnleash
import io.getunleash.strategy.Strategy
import io.getunleash.util.UnleashConfig
import java.net.URI
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import no.nav.aap.util.LoggerUtil
import no.nav.boot.conditionals.Cluster.Companion.currentCluster

@Configuration(proxyBeanMethods = false)
class UnleashConfig {

    @Bean
    fun unleashConfig(@Value("\${unleash.endpoint:https://unleash.nais.io/api/}") endpoint : URI) : UnleashConfig {
        return UnleashConfig.builder()
            .appName("aap-routing")
            .unleashAPI(endpoint)
            .build()
    }

    @Bean
    fun unleash(cfg : UnleashConfig) = DefaultUnleash(cfg, ByClusterStrategy())
}

private class ByClusterStrategy : Strategy {

    private val log = LoggerUtil.getLogger(ByClusterStrategy::class.java)

    override fun getName() = "byCluster"

    override fun isEnabled(parameters : Map<String, String>) : Boolean {
        log.info("Parameters $parameters")
        return currentCluster.name.equals(parameters["cluster"], ignoreCase = true)
    }
}