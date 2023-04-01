package no.nav.aap.fordeling.slack

import com.slack.api.Slack
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.DefaultValue
import org.springframework.stereotype.Component
import no.nav.aap.fordeling.slack.SlackConfig.Companion.ERROR
import no.nav.aap.fordeling.slack.SlackConfig.Companion.OK
import no.nav.aap.fordeling.slack.SlackConfig.Companion.ROCKET
import no.nav.aap.fordeling.slack.SlackConfig.Companion.SLACK
import no.nav.aap.util.LoggerUtil.getLogger
import no.nav.boot.conditionals.Cluster.Companion.currentCluster
import no.nav.boot.conditionals.Cluster.Companion.devClusters

@Component
class Slacker(private val cfg : SlackConfig) {

    private val log = getLogger(Slacker::class.java)
    
    fun okHvisDev(melding : String) =
        if (cluster in devClusters()) {
            ok(melding)
        }
        else Unit

    fun feilHvisDev(melding : String) =
        if (cluster in devClusters()) {
            feil(melding)
        }
        else Unit

    fun meldingHvisDev(melding : String) =
        if (cluster in devClusters()) {
            rocket(melding)
        }
        else Unit

    fun ok(melding : String) = send("$OK$melding")

    fun feil(melding : String) = send("$ERROR$melding")

    fun rocket(melding : String) = send("$ROCKET$melding")

    private fun send(melding : String) =
        with(cfg) {
            if (!enabled) {
                log.warn("Sending til slack ikke aktivert, sett slack.enabled: true for aktivering")
                return
            }

            runCatching {
                with(slack.methods(token).chatPostMessage {
                    it.channel(kanal).text(melding + " (Cluster: ${currentCluster().name.lowercase()})")
                }) {
                    if (!isOk) {
                        log.warn("Klarte ikke sende melding til Slack-kanal: $kanal. Fikk respons $this")
                    }
                }
            }.getOrElse {
                log.warn("Fikk ikke kontakt med Slack-API", it)
            }
        }

    companion object {

        private val cluster = currentCluster()
        private val slack = Slack.getInstance()
    }
}

@ConfigurationProperties(SLACK)
data class SlackConfig(val kanal : String, val token : String, @DefaultValue("true") val enabled : Boolean) {

    companion object {

        private fun String.emoji() = ":$this: "
        const val SLACK = "slack"
        val ROCKET = "rocket".emoji()
        val ERROR = "error".emoji()
        val OK = "white_check_mark".emoji()
    }
}