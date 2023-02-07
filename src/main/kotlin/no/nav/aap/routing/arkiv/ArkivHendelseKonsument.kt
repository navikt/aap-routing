package no.nav.aap.routing.arkiv

import no.nav.aap.api.felles.error.IntegrationException
import no.nav.aap.routing.arkiv.Fordeler.FordelingResultat
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
class ArkivHendelseKonsument(private val delegator: RutingDelegator) {

    @KafkaListener(topics = ["#{'\${joark.hendelser.topic:teamdokumenthandtering.aapen-dok-journalfoering}'}"], containerFactory = JOARK)
    fun listen(@Payload payload: JournalfoeringHendelseRecord)  = delegator.deleger(payload.journalpostId, payload.temaNytt)
}

@Component
class RutingDelegator(private val cfg: FordelingConfigurationProperties, val arkiv: ArkivClient,private val fordeler: List<Fordeler>) {
    private val log = getLogger(javaClass)
    fun deleger(id: Long, tema: String) =
        arkiv.journalpost(id)?.let {jp ->
            cfg.routing[tema.lowercase()]?.let { c ->
                if (jp.journalstatus in c.statuser && jp.dokumenter.any { it.brevkode in c.brevkoder})  {
                    log.info("Fordeler $this (snart)")
                    fordeler.find { it.mode() == c.mode }
                        ?.fordel(jp) ?: log.warn("Fant ingen ruter for tema $tema og mode ${c.mode}")
                }
                else {
                    log.info("Journalpost $jp for $tema rutes ikke")
                }
            } ?: log.info("Fant ingen konfigurasjon for $tema, kjenner kun til følgende:  ${cfg.routing.keys}")
        } ?: log.info("Ingen journalpost for id $id")
}


@Component
class LegacyAAPFordeler(private val integrator: Integrator) : Fordeler {
    override fun mode() = "legacy"
    override fun fordel(journalpost: Journalpost): FordelingResultat {
           integrator.slåOpp(journalpost)
        return FordelingResultat("OK")
    }
}


interface Fordeler {
    fun mode(): String
    fun fordel(journalpost: Journalpost) : FordelingResultat
    data class FordelingResultat(val status: String)
}

@Component
class Integrator(private val integrasjoner: Integrasjoner) {
    fun slåOpp(jp: Journalpost) =
        runCatching {
            with(integrasjoner) {
                    OppslagResultat(jp, org.navEnhet(pdl.geoTilknytning(jp.fnr), egen.erSkjermet(jp.fnr), pdl.diskresjonskode(jp.fnr)))
                } ?: throw IntegrationException("Ingen journalpost")
        }.getOrThrow()

    data class OppslagResultat(val journalpost: Journalpost,  val enhet: NavEnhet)
}

@Component
data class Integrasjoner(val pdl: PDLClient, val org: NavOrgClient, val egen: EgenAnsattClient)