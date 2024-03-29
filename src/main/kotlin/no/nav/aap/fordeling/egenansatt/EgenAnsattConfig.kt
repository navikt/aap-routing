package no.nav.aap.fordeling.egenansatt

import java.net.URI
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.web.util.UriBuilder
import no.nav.aap.fordeling.egenansatt.EgenAnsattConfig.Companion.EGENANSATT
import no.nav.aap.rest.AbstractRestConfig

@ConfigurationProperties(EGENANSATT)
class EgenAnsattConfig(baseUri : URI, enabled : Boolean = true, pingPath : String = DEFAULT_PING_PATH, val path : String = SKJERMING_PATH)
    : AbstractRestConfig(baseUri, pingPath, EGENANSATT, enabled) {

    fun skjermetUri(b : UriBuilder) = b.path(path).build()

    override fun toString() = "${javaClass.simpleName} [pingPath=$pingPath,enabled=$isEnabled,baseUri=$baseUri]"

    companion object {

        private const val DEFAULT_PING_PATH = "internal/health/liveness"
        private const val SKJERMING_PATH = "skjermet"
        const val EGENANSATT = "egenansatt"
    }
}