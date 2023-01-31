package no.nav.aap.routing.arkiv

import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.routing.navorganisasjon.NavOrgWebClientAdapter
import no.nav.aap.routing.person.PDLWebClientAdapter
import no.nav.aap.util.Constants.JOARK
import no.nav.aap.util.LoggerUtil.getLogger
import no.nav.boot.conditionals.ConditionalOnGCP
import no.nav.joarkjournalfoeringhendelser.JournalfoeringHendelseRecord
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.messaging.handler.annotation.Payload

@ConditionalOnGCP
class ArkivHendelseKonsument(private val client: ArkivClient, private val pdl: PDLWebClientAdapter, val navOrg: NavOrgWebClientAdapter) {
    private val log = getLogger(javaClass)

    @KafkaListener(topics = ["#{'\${joark.hendelser.topic:teamdokumenthandtering.aapen-dok-journalfoering}'}"], containerFactory = JOARK)
    fun listen(@Payload payload: JournalfoeringHendelseRecord)  =
        client.journalpost(payload.journalpostId).also {  // map til domeneobjekt
            log.info("Payload $payload mottatt, respons SAF $it")
            pdl.geoTilknytning(Fødselsnummer("08089403198")).also {
                log.info("PDL respons $it")
            }
        }
}