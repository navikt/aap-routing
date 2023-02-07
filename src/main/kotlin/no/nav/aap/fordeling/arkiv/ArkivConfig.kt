package no.nav.aap.fordeling.arkiv

import java.net.URI
import no.nav.aap.rest.AbstractRestConfig
import no.nav.aap.rest.AbstractRestConfig.RetryConfig.Companion.DEFAULT
import no.nav.aap.util.Constants.JOARK
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.NestedConfigurationProperty
import org.springframework.boot.context.properties.bind.DefaultValue

@ConfigurationProperties(JOARK)
class ArkivConfig(
        @DefaultValue(DEFAULT_PING_PATH) pingPath: String,
        @DefaultValue("true") enabled: Boolean,
        @NestedConfigurationProperty private val retryCfg: RetryConfig = DEFAULT,
        @NestedConfigurationProperty val hendelser: HendelseConfig,
        baseUri: URI) : AbstractRestConfig(baseUri, pingPath, JOARK, enabled,retryCfg) {


    data class HendelseConfig(val topic: String)


    override fun toString() = "${javaClass.simpleName} [pingPath=$pingPath,enabled=$isEnabled,baseUri=$baseUri]"

    companion object {
        private const val DEFAULT_PING_PATH = "isAlive"
    }
}