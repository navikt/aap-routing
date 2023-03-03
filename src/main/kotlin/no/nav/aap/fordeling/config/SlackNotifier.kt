package no.nav.aap.fordeling.config

import com.slack.api.Slack
import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
class SlackNotifier(private val cfg: SlackConfig) {

    fun sendMessage(message: String) =
        with(cfg) {
            if (enabled) {
                runCatching {
                    with(slack.methods(token).chatPostMessage {
                        it.channel(kanal).text(message)
                    }) {
                        if (!isOk) {
                            log.warn("Klarte ikke sende melding til Slack-kanal: $kanal. Skulle sendt melding: '$message'", errors.joinToString("\n"))
                        }
                    }
                }.getOrElse{
                    log.warn("Fikk ikke kontakt med Slack sitt API. Skulle ha sendt melding: '$message'", it)
                }
            }
            else {
                log.warn("Sending til slack ikke aktivert, set 'slack.enabled: true for aktivering")
            }
        }

    companion object {
        private val log = LoggerFactory.getLogger(SlackNotifier::class.java)
        private val slack = Slack.getInstance()
    }
}

@ConfigurationProperties("slack")
data class SlackConfig(val kanal: String, val token: String, val enabled: Boolean)