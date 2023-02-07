package no.nav.aap.fordeling.arkiv

import no.nav.aap.api.felles.error.IntegrationException
import no.nav.aap.fordeling.arkiv.Fordeler.FordelingResultat
import no.nav.aap.fordeling.egenansatt.EgenAnsattClient
import no.nav.aap.fordeling.navorganisasjon.NavEnhet
import no.nav.aap.fordeling.navorganisasjon.NavOrgClient
import no.nav.aap.fordeling.person.PDLClient
import no.nav.aap.util.Constants.JOARK
import no.nav.aap.util.LoggerUtil.getLogger
import no.nav.boot.conditionals.ConditionalOnGCP
import no.nav.joarkjournalfoeringhendelser.JournalfoeringHendelseRecord
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Component

@ConditionalOnGCP
class ArkivHendelseKonsument(private val delegator: FordelingsDelegator) {

    @KafkaListener(topics = ["#{'\${joark.hendelser.topic:teamdokumenthandtering.aapen-dok-journalfoering}'}"], containerFactory = JOARK)
    fun listen(@Payload payload: JournalfoeringHendelseRecord)  = delegator.deleger(payload.journalpostId, payload.temaNytt)
}

fun FordelingConfigurationProperties.finnFordeler(jp: Journalpost, tema: String, fordelere: List<Fordeler>):Fordeler?  {
    val log = getLogger(javaClass)
    return routing[tema.lowercase()]?.let { c ->
        if (jp.journalstatus in c.statuser && jp.dokumenter.any { it.brevkode in c.brevkoder }) {
            fordelere.find { it.mode() == c.mode }
        } else {
            log.info("Journalpost $jp for $tema rutes ikke")
            null
        }
    } ?: null
}

@Component
class FordelingsDelegator(private val cfg: FordelingConfigurationProperties, val arkiv: ArkivClient, private val fordeler: List<Fordeler>) {
    private val log = getLogger(javaClass)
    fun deleger(id: Long, tema: String) =
        arkiv.journalpost(id)?.let {jp ->
            cfg.finnFordeler(jp,tema,fordeler)?.fordel(jp)?: log.warn("Fant ingen ruter for tema $tema")
                }?: log.info("Ingen journalpost for id $id")
}


@Component
class LegacyAAPFordeler(private val integrator: Integrator) : Fordeler {

    private val log = getLogger(javaClass)

    override fun mode() = "legacy"
    override fun fordel(journalpost: Journalpost): FordelingResultat {
           integrator.slåOpp(journalpost)
        log.info("Fordeler $journalpost")
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