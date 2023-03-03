package no.nav.aap.fordeling.arkiv

import java.net.URI
import no.nav.aap.fordeling.arkiv.ArkivConfig.Companion.ROUTING
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
        val dokarkiv: URI,
        @DefaultValue(DEFAULT_FERDIGSTILL_PATH) val ferdigstillPath: String,
        @DefaultValue(DEFAULT_OPPDATER_PATH) val oppdaterPath: String,
        @NestedConfigurationProperty val hendelser: TopicConfig,
        baseUri: URI) : AbstractRestConfig(baseUri, pingPath, JOARK, enabled,DEFAULT) {


    data class TopicConfig(val topic: String)

    override fun toString() = "${javaClass.simpleName} [pingPath=$pingPath,enabled=$isEnabled,baseUri=$baseUri]"

    companion object {
        const val ROUTING = "routing"
        const val DOKARKIV = "dokarkiv"
        private const val PATH_PREFIX = "/rest/journalpostapi/v1/journalpost/"
        private const val DEFAULT_FERDIGSTILL_PATH = "$PATH_PREFIX{journalpostid}/ferdigstill"
        private const val DEFAULT_OPPDATER_PATH = "$PATH_PREFIX{journalpostid}"
        private const val DEFAULT_PING_PATH = "isAlive"
    }
}
@ConfigurationProperties(ROUTING)
data class RoutingConfig(val retry: String, val dlt: String)