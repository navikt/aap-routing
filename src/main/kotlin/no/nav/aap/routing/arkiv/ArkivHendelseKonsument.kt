package no.nav.aap.routing.arkiv

import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.routing.navorganisasjon.EnhetsKriteria
import no.nav.aap.routing.navorganisasjon.NavOrgClient
import no.nav.aap.routing.person.PDLClient
import no.nav.aap.routing.person.PDLGeoTilknytning
import no.nav.aap.util.Constants.JOARK
import no.nav.aap.util.LoggerUtil.getLogger
import no.nav.boot.conditionals.ConditionalOnGCP
import no.nav.joarkjournalfoeringhendelser.JournalfoeringHendelseRecord
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Component

@ConditionalOnGCP
class ArkivHendelseKonsument(private val fordeler: Fordeler) {

    @KafkaListener(topics = ["#{'\${joark.hendelser.topic:teamdokumenthandtering.aapen-dok-journalfoering}'}"], containerFactory = JOARK)
    fun listen(@Payload payload: JournalfoeringHendelseRecord)  =
        fordeler.fordel(payload.journalpostId)
}

@Component
class Fordeler(private val oppslag: Oppslager) {
    private val log = getLogger(javaClass)
    fun fordel(id: Long){
        oppslag.slåOpp(id).also {
            log.info("Fordeler $it")
        }
    }
}
@Component
class Oppslager(private val clients: Clients) {
    fun slåOpp(journalpost: Long) =
        with(clients) {
            OppslagResultat(arkiv.journalpost(journalpost),pdl.geoTilknytning(Fødselsnummer("08089403198")),org.bestMatch(EnhetsKriteria("030107")))
        }
    data class OppslagResultat(val journalpost: Journalpost?, val geo: PDLGeoTilknytning?, val org: Map<String,Any>)
}

@Component
class Clients(val arkiv: ArkivClient, val pdl: PDLClient, val org: NavOrgClient)