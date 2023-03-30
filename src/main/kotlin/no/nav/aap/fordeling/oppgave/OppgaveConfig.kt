package no.nav.aap.fordeling.oppgave

import java.net.URI
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.util.UriBuilder
import no.nav.aap.fordeling.oppgave.OppgaveConfig.Companion.OPPGAVE
import no.nav.aap.fordeling.oppgave.OppgaveType.FORDELINGSOPPGAVE
import no.nav.aap.fordeling.oppgave.OppgaveType.JOURNALFØRINGSOPPGAVE
import no.nav.aap.rest.AbstractRestConfig

@ConfigurationProperties(OPPGAVE)
class OppgaveConfig(
    baseUri : URI,
    pingPath : String = DEFAULT_PING_PATH,
    enabled : Boolean = false,
    val oppslagEnabled : Boolean = true,
    val oppgavePath : String = DEFAULT_OPPGAVE_PATH) : AbstractRestConfig(baseUri, pingPath, OPPGAVE, enabled) {

    fun oppgaveUri(b : UriBuilder, id : String) =
        b.queryParams(OPPGAVE_PARAMS).queryParam(JOURNALPOSTID, id).path(oppgavePath).build()

    fun opprettOppgaveUri(b : UriBuilder) = b.path(oppgavePath).build()

    override fun toString() = "OppgaveConfig(oppslagEnabled=$oppslagEnabled, oppgavePath='$oppgavePath'), ${super.toString()})"

    companion object {

        const val OPPGAVE = "oppgave"
        private const val DEFAULT_PING_PATH = "internal/alive"
        private const val DEFAULT_OPPGAVE_PATH = "api/v1/oppgaver"
        private const val STATUSKATEGORI = "statuskategori"
        private const val OPPGAVETYPE = "oppgavetype"
        private const val JOURNALPOSTID = "journalpostId"
        private const val ÅPEN = "AAPEN"
        private val OPPGAVE_PARAMS = LinkedMultiValueMap<String, String>().apply {
            add(STATUSKATEGORI, ÅPEN)
            add(OPPGAVETYPE, JOURNALFØRINGSOPPGAVE.verdi)
            add(OPPGAVETYPE, FORDELINGSOPPGAVE.verdi)
        }
    }
}