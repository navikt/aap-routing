package no.nav.aap.routing.arkiv

import jakarta.validation.Valid
import jakarta.validation.constraints.NotEmpty
import no.nav.aap.api.felles.SkjemaType
import no.nav.aap.api.felles.SkjemaType.*
import no.nav.aap.api.felles.error.IntegrationException
import no.nav.aap.routing.arkiv.JournalpostDTO.JournalStatus
import no.nav.aap.routing.arkiv.JournalpostDTO.JournalStatus.MOTTATT
import no.nav.aap.routing.egenansatt.EgenAnsattClient
import no.nav.aap.routing.navorganisasjon.NavEnhet
import no.nav.aap.routing.navorganisasjon.NavOrgClient
import no.nav.aap.routing.person.PDLClient
import no.nav.aap.util.Constants.JOARK
import no.nav.aap.util.LoggerUtil.getLogger
import no.nav.boot.conditionals.ConditionalOnGCP
import no.nav.joarkjournalfoeringhendelser.JournalfoeringHendelseRecord
import no.nav.security.token.support.client.core.ClientProperties
import org.springframework.boot.context.properties.ConfigurationProperties
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
    fun fordel(id: Long) =
        with(integrator.slåOpp(id)) {
            if (MOTTATT == journalpost.journalstatus  && journalpost.dokumenter.any { it.brevkode in listOf(
                        STANDARD.kode,
                        STANDARD_ETTERSENDING.kode) }) {
                log.info("Håndterer $this (snart)")  // TODO gjør konfigurerbart pr tema og brevkode
            }
            else  {
                log.info("Ignorerer ${journalpost.journalstatus}")
            }
        }
}

@ConfigurationProperties("fordeling")
data class FordelingConfigurationProperties(val routing: @NotEmpty Map<String, FordelingProperties>) {

    data class FordelingProperties(val mode: String, val statuser: List<JournalStatus>,val brevkoder: List<String>)

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