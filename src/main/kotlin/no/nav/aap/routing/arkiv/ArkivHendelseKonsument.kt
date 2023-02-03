package no.nav.aap.routing.arkiv

import no.nav.aap.routing.egenansatt.EgenAnsattClient
import no.nav.aap.routing.navorganisasjon.EnhetsKriteria
import no.nav.aap.routing.navorganisasjon.NavOrgClient
import no.nav.aap.routing.person.PDLClient
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
    fun listen(@Payload payload: JournalfoeringHendelseRecord)  = fordeler.fordel(payload.journalpostId)
}

@Component
class Fordeler(private val oppslag: Oppslager) {
    private val log = getLogger(javaClass)
    fun fordel(id: Long){
        oppslag.slåOpp(id).also {
            log.info("Fordeler $it (snart)")
        }
    }
}
@Component
class Oppslager(private val clients: Clients) {

    private val log = getLogger(javaClass)

    fun slåOpp(journalpost: Long) =
        with(clients) {
            arkiv.journalpost(journalpost)?.let { jp ->
                egen.erSkjermet(jp.fnr).also{ log.info("Skjerming status $it") }
                //pdl.diskresjonskode(jp.fnr).also { log.info("Diskresjonskode $it") }
                pdl.geoTilknytning(jp.fnr).also{ log.info("GEO status $it") }
                    //?.let { g  ->
               //     OppslagResultat(jp,g, org.bestMatch(EnhetsKriteria(g)))
               // } ?: log.warn("Null fra GT oppslag")
            } ?: log.warn("Null fra journalpost oppslag")
        }
    data class OppslagResultat(val journalpost: Journalpost, val gt: String?, val org: Map<String,Any>)
}

@Component
class Clients(val arkiv: ArkivClient, val pdl: PDLClient, val org: NavOrgClient, val egen: EgenAnsattClient)