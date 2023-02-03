package no.nav.aap.routing.arkiv

import no.nav.aap.routing.egenansatt.EgenAnsattClient
import no.nav.aap.routing.navorganisasjon.NavEnhet
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
        runCatching {
            with(clients) {
                arkiv.journalpost(journalpost)?.let { jp ->
                    with(pdl.geoTilknytning(jp.fnr))  {
                        OppslagResultat(jp, this, org.navEnhet(this, egen.erSkjermet(jp.fnr), pdl.diskresjonskode(jp.fnr)))
                    }
                } ?: log.warn("Ingen Journalpost")
            }
        }.getOrThrow()

    data class OppslagResultat(val journalpost: Journalpost, val gt: String?, val enhet: NavEnhet)
}

@Component
class Clients(val arkiv: ArkivClient, val pdl: PDLClient, val org: NavOrgClient, val egen: EgenAnsattClient)