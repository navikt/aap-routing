package no.nav.aap.fordeling.arkiv

import no.nav.aap.api.felles.error.IntegrationException
import no.nav.aap.fordeling.arkiv.Fordeler.FordelingResultat
import no.nav.aap.fordeling.egenansatt.EgenAnsattClient
import no.nav.aap.fordeling.navorganisasjon.NavEnhet
import no.nav.aap.fordeling.navorganisasjon.NavOrgClient
import no.nav.aap.fordeling.person.PDLClient
import no.nav.aap.util.Constants.AAP
import no.nav.aap.util.Constants.JOARK
import no.nav.aap.util.LoggerUtil.getLogger
import no.nav.boot.conditionals.ConditionalOnGCP
import no.nav.joarkjournalfoeringhendelser.JournalfoeringHendelseRecord
import org.apache.avro.Protocol.Message
import org.springframework.kafka.annotation.DltHandler
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.annotation.RetryableTopic
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.kafka.support.KafkaHeaders.*
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Component

@ConditionalOnGCP
class ArkivHendelseKonsument(private val delegator: DelegerendeFordeler) {

    val log = getLogger(javaClass)


    @KafkaListener(topics = ["#{'\${joark.hendelser.topic:teamdokumenthandtering.aapen-dok-journalfoering}'}"], containerFactory = JOARK)
    fun listen(@Payload payload: JournalfoeringHendelseRecord)  {
        delegator.deleger(payload.journalpostId, payload.temaNytt)
    }

    @KafkaListener(topics =["aap.routingdlt"])
    fun dltHander(@Payload payload: JournalfoeringHendelseRecord,
                  @Header(ORIGINAL_OFFSET) offset:  ByteArray,
                  @Header(EXCEPTION_FQCN) descException: String,
                  @Header(EXCEPTION_STACKTRACE) stacktrace: String,
                  @Header(EXCEPTION_MESSAGE) errorMessage: String)   {
        log.info("OOPS, DEAD LETTER $payload")
    }
}

@Component
class DelegerendeFordeler(private val cfg: FordelingConfigurationProperties, val arkiv: ArkivClient, private val fordelere: List<Fordeler>) {
    private val log = getLogger(javaClass)
    fun deleger(id: Long, tema: String) =
        arkiv.journalpost(id)?.let {jp ->
            cfg.finnFordeler(jp,tema,fordelere)?.fordel(jp)?: log.warn("Fant ingen ruter for tema $tema")
                }?: log.info("Ingen journalpost for id $id")
}


@Component
class AAPFordeler(private val integrator: Integrator) : Fordeler {

    private val log = getLogger(javaClass)
    override fun tema() = AAP
    override fun fordel(journalpost: Journalpost): FordelingResultat {
           integrator.slåOpp(journalpost)
        log.info("Fordeler $journalpost")
        throw IntegrationException("TESTING 123")
        //return FordelingResultat("OK")
    }
}


interface Fordeler {
    fun tema(): String
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