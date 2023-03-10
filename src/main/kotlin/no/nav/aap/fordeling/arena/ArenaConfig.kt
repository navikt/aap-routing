package no.nav.aap.fordeling.arena

import java.net.URI
import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.fordeling.arena.ArenaConfig.Companion.ARENA
import no.nav.aap.rest.AbstractRestConfig
import no.nav.aap.rest.AbstractRestConfig.RetryConfig.Companion.DEFAULT
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.DefaultValue
import org.springframework.web.util.UriBuilder

@ConfigurationProperties(ARENA)
class ArenaConfig(
        @DefaultValue(DEFAULT_PING_PATH) pingPath: String,
        @DefaultValue("true") enabled: Boolean,
        @DefaultValue(NYESTE_PATH) val nyesteSakPath: String,
        @DefaultValue(OPPGAVE_PATH) val oppgavePath: String,
        baseUri: URI) : AbstractRestConfig(baseUri, pingPath, ARENA, enabled, DEFAULT) {

    fun nyesteSakUri(b: UriBuilder, fnr: Fødselsnummer) = b.path(nyesteSakPath).build(fnr.fnr)
    fun oppgaveUri(b: UriBuilder) = b.path(oppgavePath).build()

    override fun toString() = "${javaClass.simpleName} [pingPath=$pingPath,enabled=$isEnabled,baseUri=$baseUri]"

    companion object {
        private const val NYESTE_PATH = "arena/nyesteaktivesak/{fnr}"
        private const val OPPGAVE_PATH = "arena/opprettoppgave"
        private const val DEFAULT_PING_PATH = "actuator/health/liveness"
        const val ARENA = "arena"
    }
}