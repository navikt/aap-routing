package no.nav.aap.routing.arena

import java.net.URI
import no.nav.aap.rest.AbstractRestConfig
import no.nav.aap.rest.AbstractRestConfig.RetryConfig.Companion.DEFAULT
import no.nav.aap.routing.arena.ArenaConfig.Companion.ARENA
import no.nav.aap.routing.egenansatt.EgenAnsattConfig.Companion.EGENANSATT
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.NestedConfigurationProperty
import org.springframework.boot.context.properties.bind.DefaultValue

@ConfigurationProperties(ARENA)
class ArenaConfig(
    @DefaultValue(DEFAULT_PING_PATH) pingPath: String,
    @DefaultValue("true") enabled: Boolean,
    @NestedConfigurationProperty private val retryCfg: RetryConfig = DEFAULT,
    baseUri: URI) : AbstractRestConfig(baseUri, pingPath, ARENA, enabled,retryCfg) {
    override fun toString() = "${javaClass.simpleName} [pingPath=$pingPath,enabled=$isEnabled,baseUri=$baseUri]"

    companion object {
        private const val DEFAULT_PING_PATH = "internal/health/liveness"
        const val ARENA = "arena"
    }
}