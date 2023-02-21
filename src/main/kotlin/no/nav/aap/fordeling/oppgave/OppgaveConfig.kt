package no.nav.aap.fordeling.oppgave

import java.net.URI
import no.nav.aap.fordeling.egenansatt.EgenAnsattConfig.Companion.EGENANSATT
import no.nav.aap.fordeling.oppgave.OppgaveConfig.Companion.OPPGAVE
import no.nav.aap.rest.AbstractRestConfig
import no.nav.aap.rest.AbstractRestConfig.RetryConfig.Companion.DEFAULT
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.NestedConfigurationProperty
import org.springframework.boot.context.properties.bind.DefaultValue

@ConfigurationProperties(OPPGAVE)
class OppgaveConfig(
    @DefaultValue(DEFAULT_PING_PATH) pingPath: String,
    @DefaultValue("true") enabled: Boolean,
    @DefaultValue(OPPGAVE_PATH) val path: String,
    baseUri: URI) : AbstractRestConfig(baseUri, pingPath, OPPGAVE, enabled, DEFAULT) {


    override fun toString() = "${javaClass.simpleName} [pingPath=$pingPath,enabled=$isEnabled,baseUri=$baseUri]"

    companion object {
        private const val DEFAULT_PING_PATH = "TODO"
        private const val OPPGAVE_PATH = "/api/v1/oppgaver"
        const val OPPGAVE = "oppgave"
    }
}