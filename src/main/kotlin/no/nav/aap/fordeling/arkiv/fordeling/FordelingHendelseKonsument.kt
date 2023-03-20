package no.nav.aap.fordeling.arkiv.fordeling

import no.nav.aap.api.felles.error.IrrecoverableIntegrationException
import no.nav.aap.fordeling.arkiv.ArkivClient
import no.nav.aap.fordeling.arkiv.fordeling.FordelingConfig.Companion.FORDELING
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.FordelingResultat.FordelingType.INGEN
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.JournalpostDTO.JournalStatus.MOTTATT
import no.nav.aap.fordeling.util.MetrikkLabels.BREVKODE
import no.nav.aap.fordeling.util.MetrikkLabels.FORDELINGSTYPE
import no.nav.aap.fordeling.util.MetrikkLabels.FORDELINGTS
import no.nav.aap.fordeling.util.MetrikkLabels.KANAL
import no.nav.aap.fordeling.util.MetrikkLabels.TITTEL
import no.nav.aap.fordeling.egenansatt.EgenAnsattClient
import no.nav.aap.fordeling.navenhet.NavEnhetUtvelger
import no.nav.aap.fordeling.slack.Slacker
import no.nav.aap.util.ChaosMonkey
import no.nav.aap.util.Constants.TEMA
import no.nav.aap.util.EnvExtensions.isProd
import no.nav.aap.util.LoggerUtil.getLogger
import no.nav.aap.util.Metrikker
import no.nav.boot.conditionals.Cluster
import no.nav.boot.conditionals.Cluster.Companion.isProd
import no.nav.boot.conditionals.ConditionalOnGCP
import no.nav.joarkjournalfoeringhendelser.JournalfoeringHendelseRecord
import org.springframework.kafka.annotation.DltHandler
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.annotation.RetryableTopic
import org.springframework.kafka.retrytopic.RetryTopicHeaders.DEFAULT_HEADER_ATTEMPTS
import org.springframework.kafka.retrytopic.SameIntervalTopicReuseStrategy.SINGLE_TOPIC
import org.springframework.kafka.support.KafkaHeaders.*
import org.springframework.messaging.handler.annotation.Header
import org.springframework.retry.annotation.Backoff

@ConditionalOnGCP
class FordelingHendelseKonsument(
        private val factory: FordelingFactory,
        private val arkiv: ArkivClient,
        private val enhet: NavEnhetUtvelger,
        private val slack: Slacker,
        private val egen: EgenAnsattClient, //TODO Midlertidig
        private val monkey: ChaosMonkey) {

    val log = getLogger(FordelingHendelseKonsument::class.java)

    @KafkaListener(topics = ["#{'\${fordeling.topics.main}'}"], containerFactory = FORDELING)
    @RetryableTopic(attempts = "#{'\${fordeling.topics.retries}'}", backoff = Backoff(delayExpression = "#{'\${fordeling.topics.backoff}'}"),
            sameIntervalTopicReuseStrategy = SINGLE_TOPIC,
            exclude = [IrrecoverableIntegrationException::class],
            autoStartDltHandler = "true",
            autoCreateTopics = "false")
    fun listen(h: JournalfoeringHendelseRecord, @Header(DEFAULT_HEADER_ATTEMPTS, required = false) n: Int?, @Header(RECEIVED_TOPIC) topic: String) {
        runCatching {
           // monkey.inhjectFault(this)
            log.info("Mottatt journalpost ${h.journalpostId} med tema ${h.tema()} på $topic for ${n?.let { "$it." } ?: "1."} gang.")
            val jp = arkiv.hentJournalpost("${h.journalpostId}")

            if (jp == null)  {
                log.warn("Ingen journalpost, lar dette fanges opp av sikkerhetsnettet")
                return
            }

            if (jp.bruker == null) {
                log.warn("Ingen bruker er satt på journalposten, går direkte til manuell journalføring (snart)")
               // return fordeler.fordelManuelt(jp, FORDELINGSENHET)
                return
            }

            lagMetrikker(jp)
            if (isProd()) {
               throw IrrecoverableIntegrationException("Test resilience")
                egen.erSkjermet(jp.fnr)  // Resilience test web client
                log.info("Prematur retur i prod for Journalpost $jp")
                return  // TODO Midlertidig
            }

            jp.run {
                if (factory.isEnabled() && status == MOTTATT) {
                    factory.fordelerFor(h.tema()).fordel(this, enhet.navEnhet(this)).also {
                        with("${it.msg()} ($fnr)") {
                            log.info(this)
                            slack.jippiHvisCluster(this)
                        }
                    }
                }
                else {
                    log.info("Ingen fordeling av $journalpostId, enten disabled eller allerede endelig journalført")
                }
            }
        }.onFailure {
            with("Fordeling av journalpost ${h.journalpostId}  feilet for ${n?.let { "$it." } ?: "1."} gang") {
                log.warn("$this ($it.javaClass.simpleName)", it)
                slack.feilHvisCluster("$this. (${it.message})")
            }
            throw it
        }
    }

    @DltHandler
    fun dlt(h: JournalfoeringHendelseRecord, @Header(EXCEPTION_STACKTRACE) trace: String?) {
        with("Gir opp fordeling av journalpost ${h.journalpostId}") {
            log.warn("$this $trace")
            slack.feil(this)
        }
    }

    private fun lagMetrikker(jp: Journalpost) {
        var tittel = jp.tittel?.let { if (it.startsWith("Meldekort for uke", ignoreCase = true)) "Meldekort" else it } ?: "Ingen tittel"
        tittel = if (tittel.startsWith("korrigert meldekort", ignoreCase = true)) "Korrigert meldekort" else tittel
        val brevkode = if (jp.hovedDokumentBrevkode.startsWith("ukjent brevkode", ignoreCase = true) && tittel.contains("meldekort",
                    ignoreCase = true)) "Meldekort"
        else jp.hovedDokumentBrevkode
        Metrikker.inc(FORDELINGTS, TEMA, jp.tema, FORDELINGSTYPE, INGEN.name,TITTEL, tittel, KANAL, jp.kanal, BREVKODE, brevkode)
    }
    private fun JournalfoeringHendelseRecord.tema() = temaNytt.lowercase()

}