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
import no.nav.boot.conditionals.Cluster
import no.nav.boot.conditionals.Cluster.Companion.currentCluster

@Component
class Slacker(private val cfg : SlackConfig) : SlackOperations {

    override fun ok(melding : String, vararg clusters : Cluster) = send("$OK$melding", clusters)

    override fun feil(melding : String, vararg clusters : Cluster) = send("$ERROR$melding", clusters)

    override fun rocket(melding : String, vararg clusters : Cluster) = send("$ROCKET$melding", clusters)

    private fun send(melding : String, clusters : Array<out Cluster>) =
        with(cfg) {
            if (!enabled) {
                log.warn("Sending til slack ikke aktivert, sett slack.enabled: true for aktivering")
                return
            }
            if (currentCluster !in clusters) {
                return
            }

            runCatching {
                with(SLACK.methods(token).chatPostMessage {
                    it.channel(kanal).text(melding)
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

        private val SLACK = Slack.getInstance()
    }
}

interface SlackOperations {

    val log get() = getLogger(SlackOperations::class.java)

    fun rocket(melding : String, vararg clusters : Cluster) = log.info(melding)
    fun feil(melding : String, vararg clusters : Cluster) = log.info(melding)
    fun ok(melding : String, vararg clusters : Cluster) = log.info(melding)
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