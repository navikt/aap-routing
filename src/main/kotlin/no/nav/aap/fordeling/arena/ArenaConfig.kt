package no.nav.aap.fordeling.arena

import java.net.URI
import no.nav.aap.rest.AbstractRestConfig
import no.nav.aap.rest.AbstractRestConfig.RetryConfig.Companion.DEFAULT
import no.nav.aap.fordeling.arena.ArenaConfig.Companion.ARENA
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.NestedConfigurationProperty
import org.springframework.boot.context.properties.bind.DefaultValue

@ConfigurationProperties(ARENA)
class ArenaConfig(
    @DefaultValue(DEFAULT_PING_PATH) pingPath: String,
    @DefaultValue("true") enabled: Boolean,
    @DefaultValue(SAKER_PATH) val sakerPath: String,
    @DefaultValue(AKTIV_SAK_PATH) val aktivSakPath: String,
    @NestedConfigurationProperty private val retryCfg: RetryConfig = DEFAULT,
    baseUri: URI) : AbstractRestConfig(baseUri, pingPath, ARENA, enabled,retryCfg) {
    override fun toString() = "${javaClass.simpleName} [pingPath=$pingPath,enabled=$isEnabled,baseUri=$baseUri]"

    companion object {
        private const val SAKER_PATH = "arena/saker/{fnr}"
        private const val AKTIV_SAK_PATH = "arena/haraktivsak/{fnr}"


        private const val DEFAULT_PING_PATH = "actuator/health/liveness"
        const val ARENA = "arena"
        const val ENHET = "Enhet"
        const val PERSON = "PERSON"

    }
}