package no.nav.aap.routing.arkiv

import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.routing.navorganisasjon.EnhetsKriteria
import no.nav.aap.routing.navorganisasjon.NavOrgClient
import no.nav.aap.routing.navorganisasjon.NavOrgWebClientAdapter
import no.nav.aap.routing.person.PDLClient
import no.nav.aap.routing.person.PDLGeoTilknytning
import no.nav.aap.routing.person.PDLWebClientAdapter
import no.nav.aap.util.Constants.JOARK
import no.nav.aap.util.LoggerUtil.getLogger
import no.nav.boot.conditionals.ConditionalOnGCP
import no.nav.joarkjournalfoeringhendelser.JournalfoeringHendelseRecord
import org.apache.coyote.http11.Constants.a
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Component

@ConditionalOnGCP
class ArkivHendelseKonsument(private val oppslag: Oppslag) {
    private val log = getLogger(javaClass)

    @KafkaListener(topics = ["#{'\${joark.hendelser.topic:teamdokumenthandtering.aapen-dok-journalfoering}'}"], containerFactory = JOARK)
    fun listen(@Payload payload: JournalfoeringHendelseRecord)  =
        oppslag.slåOpp(payload.journalpostId).also {
            log.info("Oppslag $it")
        }
}

@Component
class Oppslag(private val clients: Clients) {
    fun slåOpp(journalpost: Long) =
        with(clients) {
            OppslagResultat(arkiv.journalpost(journalpost),pdl.geoTilknytning(Fødselsnummer("08089403198")),org.bestMatch(EnhetsKriteria("030107")))
        }
    data class OppslagResultat(val journalpost: Journalpost?, val geo: PDLGeoTilknytning?, val org: Map<String,Any>)
}

@Component
class Clients(val arkiv: ArkivClient, val pdl: PDLClient, val org: NavOrgClient)