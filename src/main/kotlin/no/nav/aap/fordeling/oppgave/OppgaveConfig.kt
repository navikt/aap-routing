package no.nav.aap.fordeling.oppgave

import java.net.URI
import no.nav.aap.fordeling.oppgave.OppgaveConfig.Companion.OPPGAVE
import no.nav.aap.rest.AbstractRestConfig
import no.nav.aap.rest.AbstractRestConfig.RetryConfig.Companion.DEFAULT
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.DefaultValue
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.util.UriBuilder

@ConfigurationProperties(OPPGAVE)
class OppgaveConfig(
        @DefaultValue(DEFAULT_PING_PATH) pingPath: String,
        @DefaultValue("true") enabled: Boolean,
        @DefaultValue(DEFAULT_OPPGAVE_PATH) val oppgavePath: String,
        baseUri: URI) : AbstractRestConfig(baseUri, pingPath, OPPGAVE, enabled, DEFAULT) {
    fun oppgaveUri(b: UriBuilder, id: String) = b.queryParams(OPPGAVE_PARAMS).queryParam(JOURNALPOSTID,id).path(oppgavePath).build()
    override fun toString() = "${javaClass.simpleName} [pingPath=$pingPath,enabled=$isEnabled,baseUri=$baseUri]"

    companion object {
        const val OPPGAVE = "oppgave"
        private val OPPGAVE_PARAMS = LinkedMultiValueMap<String,String>().apply {
            add(STATUSKATEGORI, ÅPEN)
            add(OPPGAVETYPE, JOURNALFØRINGSOPPGAVE)
            add(OPPGAVETYPE, FORDELINGSOPPGAVE)
        }
        private const val DEFAULT_PING_PATH = "TODO"
        private const val DEFAULT_OPPGAVE_PATH = "/api/v1/oppgaver"
        private const val STATUSKATEGORI = "statuskategori"
        private const val OPPGAVETYPE = "oppgavetype"
        private const val JOURNALPOSTID = "journalpostId"
        private const val ÅPEN = "AAPEN"
        private const val JOURNALFØRINGSOPPGAVE = "JFR"
        private const val FORDELINGSOPPGAVE = "FDR"
    }
}