package no.nav.aap.fordeling.arena

import java.net.URI
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.web.util.UriBuilder
import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.fordeling.arena.ArenaConfig.Companion.ARENA
import no.nav.aap.rest.AbstractRestConfig

@ConfigurationProperties(ARENA)
class ArenaConfig(baseUri : URI, enabled : Boolean = false, pingPath : String = DEFAULT_PING_PATH, val oppslagEnabled : Boolean = true,
                  val nyesteSakPath : String = NYESTE_PATH, val oppgavePath : String = OPPGAVE_PATH) : AbstractRestConfig(baseUri, pingPath, ARENA, enabled) {

    fun nyesteSakUri(b : UriBuilder, fnr : Fødselsnummer) = b.path(nyesteSakPath).build(fnr.fnr)

    fun oppgaveUri(b : UriBuilder) = b.path(oppgavePath).build()

    override fun toString() = "ArenaConfig(oppslagEnabled=$oppslagEnabled, nyesteSakPath='$nyesteSakPath', oppgavePath='$oppgavePath' ${super.toString()})"

    companion object {

        private const val NYESTE_PATH = "arena/nyesteaktivesak/{fnr}"
        private const val OPPGAVE_PATH = "arena/opprettoppgave"
        private const val DEFAULT_PING_PATH = "actuator/health"
        const val ARENA = "arena"
    }
}