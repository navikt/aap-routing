package no.nav.aap.fordeling.slack

import com.slack.api.Slack
import javax.xml.transform.OutputKeys
import no.nav.aap.fordeling.slack.SlackConfig.Companion.ERROR
import no.nav.aap.fordeling.slack.SlackConfig.Companion.OK
import no.nav.aap.fordeling.slack.SlackConfig.Companion.ROCKET
import no.nav.aap.fordeling.slack.SlackConfig.Companion.SLACK
import org.slf4j.LoggerFactory.*
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.DefaultValue
import org.springframework.stereotype.Component

@Component
class SlackNotifier(private val cfg: SlackConfig) {


    fun sendOK(message: String) = send("$OK$message")
    fun sendError(message: String) = send("$ERROR$message")

    private fun send(message: String) =
        with(cfg) {
            if (enabled) {
                runCatching {
                    with(slack.methods(token).chatPostMessage {
                        it.channel(kanal).text(message)
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
        private val LOG = getLogger(SlackNotifier::class.java)
        private val slack = Slack.getInstance()
    }
}

@ConfigurationProperties(SLACK)
data class SlackConfig(val kanal: String, val token: String, @DefaultValue("true") val enabled: Boolean) {
    companion object {
        const val SLACK = "slack"
        const val ROCKET = ":rocket: "
        const val ERROR = ":error: "
        const val OK = ":white_check_mark: "

    }
}