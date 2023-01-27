package no.nav.aap.routing.arkiv

import java.time.LocalDateTime.parse
import no.nav.aap.routing.arkiv.ArkivConfig.Companion.ARKIVHENDELSER
import no.nav.aap.util.LoggerUtil.getLogger
import no.nav.aap.util.TimeExtensions.toUTC
import no.nav.boot.conditionals.ConditionalOnGCP
import no.nav.joarkjournalfoeringhendelser.JournalfoeringHendelseRecord
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.transaction.annotation.Transactional

@ConditionalOnGCP
class ArkivHendelseKonsument {
    private val log = getLogger(javaClass)

    @Transactional
    @KafkaListener(topics = ["#{'\${joark.hendelser.topic:teamdokumenthandtering.aapen-dok-journalfoering}'}"], containerFactory = ARKIVHENDELSER)
    fun listen(@Payload payload: JournalfoeringHendelseRecord)  = log.info("Payload $payload mottatt")

    private fun JournalfoeringHendelseRecord.tilUTC()  = parse(hendelsesId.substringAfter('-')).toUTC()
}