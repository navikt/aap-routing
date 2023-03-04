package no.nav.aap.fordeling.arkiv.saf

import java.net.URI
import no.nav.aap.fordeling.arkiv.saf.SafConfig.Companion.SAF
import no.nav.aap.rest.AbstractRestConfig
import no.nav.aap.rest.AbstractRestConfig.RetryConfig.Companion.DEFAULT
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.DefaultValue

@ConfigurationProperties(SAF)
class SafConfig(@DefaultValue(DEFAULT_PING_PATH) pingPath: String,
                @DefaultValue("true") enabled: Boolean,
                baseUri: URI) : AbstractRestConfig(baseUri, "", SAF, enabled,DEFAULT) {


    companion object {
        const val SAF = "saf"
        const val DEFAULT_PING_PATH = ""
    }
}