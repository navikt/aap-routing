package no.nav.aap.fordeling.arkiv

import java.net.URI
import no.nav.aap.fordeling.person.PDLConfig.Companion.DEFAULT_PING_PATH
import no.nav.aap.rest.AbstractRestConfig
import no.nav.aap.rest.AbstractRestConfig.RetryConfig.Companion.DEFAULT
import no.nav.aap.util.Constants.JOARK
import org.apache.kafka.common.config.TopicConfig
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
        private const val DEFAULT_FERDIGSTILL_PATH = "/rest/journalpostapi/v1/journalpost/{journalpostid}/ferdigstill"
        private const val DEFAULT_OPPDATER_PATH = "/rest/journalpostapi/v1/journalpost/{journalpostid}"
        private const val DEFAULT_PING_PATH = "isAlive"
    }
}