package no.nav.aap.routing.arkiv

import java.net.URI
import no.nav.aap.rest.AbstractRestConfig
import no.nav.aap.rest.AbstractRestConfig.RetryConfig.Companion.DEFAULT
import no.nav.aap.util.Constants.JOARK
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.NestedConfigurationProperty
import org.springframework.boot.context.properties.bind.DefaultValue

@ConfigurationProperties(JOARK)
class ArkivConfig(
        @DefaultValue(DEFAULT_DOKARKIV_PATH) val arkivPath: String,
        @DefaultValue(DEFAULT_PING_PATH) pingPath: String,
        @DefaultValue("true") enabled: Boolean,
        @NestedConfigurationProperty private val retryCfg: RetryConfig = DEFAULT,
        @NestedConfigurationProperty val hendelser: HendelseConfig,
        baseUri: URI) : AbstractRestConfig(baseUri, pingPath, JOARK, enabled,retryCfg) {


    data class HendelseConfig(val topic: String)


    override fun toString() = "${javaClass.simpleName} [pingPath=$pingPath,arkivPath=$arkivPath,enabled=$isEnabled,baseUri=$baseUri]"

    companion object {
        const val MOTTATT = "JournalpostMottatt"
        const val ENDELIGJOURNALFØRT = "EndeligJournalført"
        const val ARKIVHENDELSER = "joarkhendelser"
        const val CLIENT_CREDENTIALS_ARKIV = "client-credentials-arkiv"
        private const val DEFAULT_DOKARKIV_PATH = "rest/journalpostapi/v1/journalpost"
        private const val DEFAULT_PING_PATH = "actuator/health/liveness"
    }
}