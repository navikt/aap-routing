package no.nav.aap.fordeling.config

import java.io.IOException
import com.slack.api.Slack
import org.slf4j.LoggerFactory

class SlackNotifierImpl(private val token: String, private val channel: String, private val enabled: Boolean) {
    private val log = LoggerFactory.getLogger(javaClass)
    private val slack = Slack.getInstance()

    init {
        log.info("Initierer SlackNotifier for kanal: $channel")
    }

     fun sendMessage(message: String) {
        if (!enabled) {
            log.info("Sending av melding via SlackNotifier er ikke skrudd på. Enabled = $enabled")
            return
        }
        try {
            val response = slack.methods(token).chatPostMessage {
                it.channel(channel)
                    .text(message)
            }
            if (!response.isOk) {
                log.warn("Klarte ikke sende melding til Slack-kanal: $channel. Skulle sendt melding: '$message'", response.errors.joinToString("\n"))
            }
        } catch (e: IOException) {
            log.warn("Fikk ikke kontakt med Slack sitt API. Skulle ha sendt melding: '$message'", e)
        }
    }
}