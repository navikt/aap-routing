package no.nav.aap.fordeling.navorganisasjon

import java.net.URI
import no.nav.aap.rest.AbstractRestConfig
import no.nav.aap.rest.AbstractRestConfig.RetryConfig.Companion.DEFAULT
import no.nav.aap.fordeling.navorganisasjon.NavOrgConfig.Companion.NAVORG
import no.nav.aap.util.Constants.JOARK
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.NestedConfigurationProperty
import org.springframework.boot.context.properties.bind.DefaultValue

@ConfigurationProperties(NAVORG)
class NavOrgConfig(
        @DefaultValue(DEFAULT_PING_PATH) pingPath: String,
        @DefaultValue("true") enabled: Boolean,
        @NestedConfigurationProperty private val retryCfg: RetryConfig = DEFAULT,
        @DefaultValue(ENHET_PATH) val enhet: String,
        @DefaultValue(AKTIVE_PATH) val aktive: String,
        baseUri: URI) : AbstractRestConfig(baseUri, pingPath, JOARK, enabled,retryCfg) {

    override fun toString() = "${javaClass.simpleName} [pingPath=$pingPath,enabled=$isEnabled,baseUri=$baseUri]"

    companion object {
        const val NAVORG = "navorg"
        const  val ENHETSLISTE = "enhetStatusListe"
        const val AKTIV = "AKTIV"
        private const val AKTIVE_PATH = "norg2/api/v1/enhet"
        private const val DEFAULT_PING_PATH = "norg2/internal/isAlive"
        private const val ENHET_PATH = "norg2/api/v1/arbeidsfordeling/enheter/bestmatch"
    }
}