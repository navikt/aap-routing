package no.nav.aap.fordeling.arkiv

import java.lang.Exception
import no.nav.aap.util.LoggerUtil.getLogger
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.listener.ConsumerRecordRecoverer
import org.springframework.stereotype.Component

@Component
class AAPTilManualFordelingErrorHandler : ConsumerRecordRecoverer {
    val log = getLogger(javaClass)

    override fun accept(t: ConsumerRecord<*, *>, u: Exception) {
        log.warn("OOOOPPPPSSSS ${t.key().javaClass}  ${t.value().javaClass}",u)
    }
}