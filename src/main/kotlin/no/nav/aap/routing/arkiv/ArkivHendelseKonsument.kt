package no.nav.aap.routing.arkiv

import no.nav.aap.api.felles.error.IntegrationException
import no.nav.aap.routing.arkiv.JournalpostDTO.JournalStatus.MOTTATT
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
class Fordeler(private val integrator: Integrator) {
    private val log = getLogger(javaClass)
    fun fordel(id: Long){
        integrator.slåOpp(id).also {
            if (MOTTATT == it.journalpost.journalstatus) {
                log.info("Håndterer $it (snart)")
            }
            else  {
                log.info("Ignorerer ${it.journalpost.journalstatus}")
            }
        }
    }
}
@Component
class Integrator(private val integrasjoner: Integrasjoner) {
    fun slåOpp(journalpost: Long) =
        runCatching {
            with(integrasjoner) {
                arkiv.journalpost(journalpost)?.let { jp ->
                    OppslagResultat(jp, org.navEnhet(pdl.geoTilknytning(jp.fnr), egen.erSkjermet(jp.fnr), pdl.diskresjonskode(jp.fnr)))
                } ?: throw IntegrationException("Ingen journalpost")
            }
        }.getOrThrow()

    data class OppslagResultat(val journalpost: Journalpost,  val enhet: NavEnhet)
}

@Component
data class Integrasjoner(val arkiv: ArkivClient, val pdl: PDLClient, val org: NavOrgClient, val egen: EgenAnsattClient)