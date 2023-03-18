package no.nav.aap.fordeling.egenansatt

import java.net.URI
import java.time.Duration
import java.time.Duration.ofSeconds
import no.nav.aap.fordeling.egenansatt.EgenAnsattConfig.Companion.EGENANSATT
import no.nav.aap.rest.AbstractRestConfig
import no.nav.aap.rest.AbstractRestConfig.RetryConfig.Companion.DEFAULT
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.DefaultValue
import org.springframework.web.util.UriBuilder

@ConfigurationProperties(EGENANSATT)
class EgenAnsattConfig(
        @DefaultValue(DEFAULT_PING_PATH) pingPath: String,
        @DefaultValue("true") enabled: Boolean,
        @DefaultValue(SKJERMING_PATH) val path: String,
        baseUri: URI) : AbstractRestConfig(baseUri, pingPath, EGENANSATT, enabled, RetryConfig(2L,ofSeconds(5))) {

    fun skjermetUri(b: UriBuilder) = b.path(path).build()
    override fun toString() = "${javaClass.simpleName} [pingPath=$pingPath,enabled=$isEnabled,baseUri=$baseUri]"

    companion object {
        private const val DEFAULT_PING_PATH = "internal/health/liveness"
        private const val SKJERMING_PATH = "skjermet"
        const val EGENANSATT = "egenansatt"
    }
}