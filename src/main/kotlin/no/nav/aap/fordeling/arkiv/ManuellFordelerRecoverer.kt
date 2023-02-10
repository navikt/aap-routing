package no.nav.aap.fordeling.arkiv

import kotlin.Exception
import no.nav.aap.util.LoggerUtil.getLogger
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.listener.ConsumerRecordRecoverer
import org.springframework.stereotype.Component

@Component
class ManuellFordelerRecoverer(private val fordeler: DelegerendeManuellFordeler) : ConsumerRecordRecoverer {
    val log = getLogger(javaClass)

    override fun accept(r: ConsumerRecord<*, *>, e: Exception) {
        when (e) {
            is FordelingException -> e.journalpost?.let {
                fordeler.fordel(it)
            } ?: log.trace("Ingen fordeling til manuell siden vi ikke har en journalpost")
            else ->  log.trace("Ingen fordeling til manuell",e)
        }
    }
}