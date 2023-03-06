package no.nav.aap.fordeling.slack

import com.slack.api.Slack
import no.nav.aap.fordeling.slack.SlackConfig.Companion.SLACK
import org.slf4j.LoggerFactory.*
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
class SlackNotifier(private val cfg: SlackConfig) {
    fun send(message: String) =
        with(cfg) {
            if (enabled) {
                runCatching {
                    with(slack.methods(token).chatPostMessage {
                        it.channel(kanal).text(message)
                    }) {
                        if (!isOk) {
                            log.warn("Klarte ikke sende melding til Slack-kanal: $kanal. Fikk respons $this")
                        }
                    }
                }.getOrElse{
                    log.warn("Fikk ikke kontakt med Slack-API", it)
                }
            }
            else {
                log.warn("Sending til slack ikke aktivert, sett slack.enabled: true for aktivering")
            }
        }

    companion object {
        private val log = getLogger(SlackNotifier::class.java)
        private val slack = Slack.getInstance()
    }
}

@ConfigurationProperties(SLACK)
data class SlackConfig(val kanal: String, val token: String, val enabled: Boolean) {
    companion object {
        const val SLACK = "slack"
    }
}