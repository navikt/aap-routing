package no.nav.aap.routing.egenansatt

import java.net.URI
import no.nav.aap.rest.AbstractRestConfig
import no.nav.aap.rest.AbstractRestConfig.RetryConfig.Companion.DEFAULT
import no.nav.aap.routing.egenansatt.EgenAnsattConfig.Companion.EGENANSATT
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.NestedConfigurationProperty
import org.springframework.boot.context.properties.bind.DefaultValue

@ConfigurationProperties(EGENANSATT)
class EgenAnsattConfig(
    @DefaultValue(DEFAULT_PING_PATH) pingPath: String,
    @DefaultValue("true") enabled: Boolean,
    @NestedConfigurationProperty private val retryCfg: RetryConfig = DEFAULT,
    @DefaultValue(SKJERMING_PATH) val path: String,
    baseUri: URI) : AbstractRestConfig(baseUri, pingPath, EGENANSATT, enabled,retryCfg) {


    override fun toString() = "${javaClass.simpleName} [pingPath=$pingPath,enabled=$isEnabled,baseUri=$baseUri]"

    companion object {
        private const val DEFAULT_PING_PATH = "internal/health/liveness"
        private const val SKJERMING_PATH = "skjermet"
        const val EGENANSATT = "skjerming"
    }
}