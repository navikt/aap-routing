package no.nav.aap.routing.skjerming

import java.net.URI
import no.nav.aap.rest.AbstractRestConfig
import no.nav.aap.rest.AbstractRestConfig.RetryConfig.Companion.DEFAULT
import no.nav.aap.routing.navorganisasjon.NavOrgConfig.Companion.ORG
import no.nav.aap.routing.skjerming.SkjermingConfig.Companion.SKJERMING
import no.nav.aap.util.Constants.JOARK
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.NestedConfigurationProperty
import org.springframework.boot.context.properties.bind.DefaultValue

@ConfigurationProperties(SKJERMING)
class SkjermingConfig(
    @DefaultValue(DEFAULT_PING_PATH) pingPath: String,
    @DefaultValue("true") enabled: Boolean,
    @NestedConfigurationProperty private val retryCfg: RetryConfig = DEFAULT,
    @DefaultValue(SKJERMING_PATH) val path: String,
    baseUri: URI) : AbstractRestConfig(baseUri, pingPath, SKJERMING, enabled,retryCfg) {


    override fun toString() = "${javaClass.simpleName} [pingPath=$pingPath,enabled=$isEnabled,baseUri=$baseUri]"

    companion object {
        private const val DEFAULT_PING_PATH = "internal/health/liveness"
        private const val SKJERMING_PATH = "skjermet"
        const val SKJERMING = "skjerming"
    }
}