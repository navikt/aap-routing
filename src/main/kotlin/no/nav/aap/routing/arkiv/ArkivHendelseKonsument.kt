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
class ArkivHendelseKonsument(private val adapter: ArkivWebClientAdapter) {
    private val log = getLogger(javaClass)

    @Transactional
    @KafkaListener(topics = ["#{'\${joark.hendelser.topic:teamdokumenthandtering.aapen-dok-journalfoering}'}"], containerFactory = ARKIVHENDELSER)
    fun listen(@Payload payload: JournalfoeringHendelseRecord)  =
        adapter.journalpost("${payload.journalpostId}").also {
            log.info("Payload $payload mottatt, respons SAF $it")
        }
}