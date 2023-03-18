package no.nav.aap.fordeling.slack

import com.slack.api.Slack
import no.nav.aap.fordeling.slack.SlackConfig.Companion.ERROR
import no.nav.aap.fordeling.slack.SlackConfig.Companion.OK
import no.nav.aap.fordeling.slack.SlackConfig.Companion.ROCKET
import no.nav.aap.fordeling.slack.SlackConfig.Companion.SLACK
import no.nav.boot.conditionals.Cluster
import no.nav.boot.conditionals.Cluster.Companion.currentCluster
import no.nav.boot.conditionals.Cluster.DEV_GCP
import org.slf4j.LoggerFactory.*
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.DefaultValue
import org.springframework.stereotype.Component

@Component
class Slacker(private val cfg: SlackConfig) {

    private val cluster = currentCluster()
    fun okHvisCluster(melding: String, vararg clusters: Cluster = arrayOf(DEV_GCP)) =
        if (cluster in clusters) {
            ok(melding)
        }
        else Unit
    fun feilHvisCluster(melding: String,vararg clusters: Cluster = arrayOf(DEV_GCP)) =
        if (cluster in clusters) {
            feil(melding)
        }
        else Unit

    fun jippiHvisCluster(melding: String,vararg clusters: Cluster = arrayOf(DEV_GCP)) =
        if (cluster in clusters) {
            jippi(melding)
        }
        else Unit

    fun ok(melding: String) = melding("$OK$melding")
    fun feil(melding: String) = melding("$ERROR$melding")
    fun jippi(melding: String) = melding("$ROCKET$melding")

    private fun melding(melding: String) =
        with(cfg) {
            if (enabled) {
                runCatching {
                    with(slack.methods(token).chatPostMessage {
                        it.channel(kanal).text(melding + " (${currentCluster().name.lowercase()}")
                    }) {
                        if (!isOk) {
                            LOG.warn("Klarte ikke sende melding til Slack-kanal: $kanal. Fikk respons $this")
                        }
                    }
                }.getOrElse {
                    LOG.warn("Fikk ikke kontakt med Slack-API", it)
                }
            }
            else {
                LOG.warn("Sending til slack ikke aktivert, sett slack.enabled: true for aktivering")
            }
        }

    companion object {
        private val LOG = getLogger(Slacker::class.java)
        private val slack = Slack.getInstance()
    }
}

@ConfigurationProperties(SLACK)
data class SlackConfig(val kanal: String, val token: String, @DefaultValue("true") val enabled: Boolean) {
    companion object {
        private fun String.emoji() = ":$this: "
        const val SLACK = "slack"
        val ROCKET = "rocket".emoji()
        val ERROR = "error".emoji()
        val OK = "white_check_mark".emoji()
    }
}