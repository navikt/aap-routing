package no.nav.aap.fordeling.arkiv

import kotlin.Exception
import no.nav.aap.fordeling.arkiv.Fordeler.FordelingResultat
import no.nav.aap.fordeling.arkiv.Tema.*
import no.nav.aap.util.LoggerUtil.getLogger
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.listener.ConsumerRecordRecoverer
import org.springframework.stereotype.Component

@Component
class AAPManuellFordeler : ConsumerRecordRecoverer, Fordeler {
    val log = getLogger(javaClass)

    override fun accept(t: ConsumerRecord<*, *>, e: Exception) {
        when (e) {
            is FordelingException -> e.journalpost?.let {
                fordel(it)
            } ?: log.trace("Ingen fordeling til manuell siden vi ikke har en journalpost")
            else ->  log.trace("Ingen fordeling til manuell",e)
        }
    }

    override fun tema() = listOf(aap)

    override fun fordel(journalpost: Journalpost): FordelingResultat {
       log.info("Fordeler manuelt $journalpost")
        return FordelingResultat("Manuell")
    }
}