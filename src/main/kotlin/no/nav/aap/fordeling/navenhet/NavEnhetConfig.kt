package no.nav.aap.fordeling.navenhet

import java.net.URI
import no.nav.aap.fordeling.navenhet.NavEnhetConfig.Companion.NAVENHET
import no.nav.aap.rest.AbstractRestConfig
import no.nav.aap.rest.AbstractRestConfig.RetryConfig.Companion.DEFAULT
import no.nav.aap.util.Constants.JOARK
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.DefaultValue

@ConfigurationProperties(NAVENHET)
class NavEnhetConfig(
        @DefaultValue(DEFAULT_PING_PATH) pingPath: String,
        @DefaultValue("true") enabled: Boolean,
        @DefaultValue(ENHET_PATH) val enhet: String,
        @DefaultValue(AKTIVE_PATH) val aktive: String,
        baseUri: URI) : AbstractRestConfig(baseUri, pingPath, JOARK, enabled, DEFAULT) {

    override fun toString() = "${javaClass.simpleName} [pingPath=$pingPath,enabled=$isEnabled,baseUri=$baseUri]"

    companion object {
        const val NAVENHET = "navorg"
        const  val ENHETSLISTE = "enhetStatusListe"
        const val AKTIV = "AKTIV"
        private const val AKTIVE_PATH = "norg2/api/v1/enhet"
        private const val DEFAULT_PING_PATH = "norg2/internal/isAlive"
        private const val ENHET_PATH = "norg2/api/v1/arbeidsfordeling/enheter/bestmatch"
    }
}