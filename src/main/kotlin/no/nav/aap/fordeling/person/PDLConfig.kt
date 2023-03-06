package no.nav.aap.fordeling.person

import java.net.URI
import no.nav.aap.fordeling.person.PDLConfig.Companion.PDL
import no.nav.aap.rest.AbstractRestConfig
import no.nav.aap.rest.AbstractRestConfig.RetryConfig.Companion.DEFAULT
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.DefaultValue

@ConfigurationProperties(PDL)
class PDLConfig(baseUri: URI, @DefaultValue("true") enabled: Boolean) :
    AbstractRestConfig(baseUri, "", PDL, enabled, DEFAULT) {

    override fun toString() = "$javaClass.simpleName [baseUri=$baseUri, pingEndpoint=$pingEndpoint]"

    companion object {
        const val PDL = "pdl"
    }
}