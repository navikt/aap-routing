package no.nav.aap.fordeling.person

import java.net.URI
import no.nav.aap.fordeling.person.PDLConfig.Companion.PDL
import no.nav.aap.rest.AbstractRestConfig
import no.nav.aap.rest.AbstractRestConfig.RetryConfig.Companion.DEFAULT
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(PDL)
class PDLConfig(baseUri : URI, enabled : Boolean = true) : AbstractRestConfig(baseUri, "", PDL, enabled, DEFAULT) {

    override fun toString() = "$javaClass.simpleName [baseUri=$baseUri, pingEndpoint=$pingEndpoint]"

    companion object {

        const val PDL = "pdl"
    }
}