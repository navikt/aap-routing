package no.nav.aap.fordeling.arkiv.saf

import java.net.URI
import no.nav.aap.fordeling.arkiv.saf.SAFConfig.Companion.SAF
import no.nav.aap.rest.AbstractRestConfig
import no.nav.aap.rest.AbstractRestConfig.RetryConfig.Companion.DEFAULT
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.DefaultValue

@ConfigurationProperties(SAF)
class SAFConfig( baseUri: URI,enabled: Boolean = true) : AbstractRestConfig(baseUri, "", SAF, enabled, DEFAULT) {
    companion object {
        const val SAF = "saf"
    }
}