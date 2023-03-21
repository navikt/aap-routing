package no.nav.aap.fordeling.arkiv.fordeling

import jakarta.validation.constraints.AssertFalse
import no.nav.aap.api.felles.error.IrrecoverableIntegrationException
import no.nav.aap.api.felles.error.RecoverableIntegrationException
import no.nav.aap.fordeling.arkiv.ArkivClient
import no.nav.aap.fordeling.arkiv.fordeling.FordelingConfig.Companion.FORDELING
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.FordelingResultat
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.FordelingResultat.FordelingType.DIREKTE_MANUELL
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.FordelingResultat.FordelingType.INGEN
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.JournalpostDTO.JournalStatus.MOTTATT
import no.nav.aap.fordeling.util.MetrikkLabels.BREVKODE
import no.nav.aap.fordeling.util.MetrikkLabels.FORDELINGSTYPE
import no.nav.aap.fordeling.util.MetrikkLabels.FORDELINGTS
import no.nav.aap.fordeling.util.MetrikkLabels.KANAL
import no.nav.aap.fordeling.util.MetrikkLabels.TITTEL
import no.nav.aap.fordeling.egenansatt.EgenAnsattClient
import no.nav.aap.fordeling.egenansatt.EgenAnsattConfig.Companion.EGENANSATT
import no.nav.aap.fordeling.navenhet.NavEnhetUtvelger
import no.nav.aap.fordeling.slack.Slacker
import no.nav.aap.util.ChaosMonkey
import no.nav.aap.util.Constants.TEMA
import no.nav.aap.util.LoggerUtil.getLogger
import no.nav.aap.util.Metrikker
import no.nav.boot.conditionals.Cluster.Companion.isProd
import no.nav.boot.conditionals.Cluster.DEV_GCP
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
import sun.jvm.hotspot.debugger.win32.coff.DebugVC50X86RegisterEnums.DI

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
            monkey.injectFault(this.javaClass.simpleName,RecoverableIntegrationException("Chaos Monkey recoverable exception"))
            log.info("Mottatt journalpost ${h.journalpostId} med tema ${h.tema()} på $topic for ${n?.let { "$it." } ?: "1."} gang.")
            val jp = arkiv.hentJournalpost("${h.journalpostId}")

            if (jp == null)  {
                log.warn("Ingen journalpost, lar dette fanges opp av sikkerhetsnettet")
                return
            }

            if (jp.bruker == null) {
                log.warn("Ingen bruker er satt på journalposten, går direkte til manuell journalføring (snart)")
                with(pair(jp)) {
                    Metrikker.inc(FORDELINGTS, TEMA, jp.tema, FORDELINGSTYPE, DIREKTE_MANUELL.name,TITTEL, first, KANAL, jp.kanal, BREVKODE, second, EGENANSATT, "false")
                }

               // return fordeler.fordelManuelt(jp, FORDELINGSENHET)
                return
            }

            lagMetrikker(jp)
            if (isProd()) {
                monkey.injectFault(this.javaClass.simpleName,IrrecoverableIntegrationException("Chaos Monkey irrecoverable exception"))
                egen.erEgenAnsatt(jp.fnr)  // Resilience test web client
                log.info("Prematur retur fra topic $topic i prod for Journalpost ${jp.journalpostId}")
                return  // TODO Midlertidig
            }

            jp.run {
                if (factory.isEnabled() && status == MOTTATT) {
                    factory.fordelerFor(h.tema()).fordel(this, enhet.navEnhet(this)).also {
                        with("${it.msg()} ($fnr)") {
                            log.info(this)
                            slack.jippiHvisCluster(this,DEV_GCP)
                        }
                    }
                }
                else {
                    log.info("Ingen fordeling av $journalpostId, enten disabled eller allerede endelig journalført")
                }
            }
        }.onFailure {
            with("Fordeling av journalpost ${h.journalpostId}  feilet for ${n?.let { "$it." } ?: "1."} gang på topic $topic") {
                log.warn("$this ($it.javaClass.simpleName)", it)
                slack.feilICluster("$this. (${it.message})", DEV_GCP)
            }
            throw it
        }
    }

    @DltHandler
    fun dlt(h: JournalfoeringHendelseRecord, @Header(EXCEPTION_STACKTRACE) trace: String?) {
        with("Gir opp fordeling av journalpost ${h.journalpostId}") {
            log.warn("$this")
            slack.feilICluster(this,DEV_GCP)
        }
    }

    private fun lagMetrikker(jp: Journalpost) =
        with(pair(jp)) {
            Metrikker.inc(FORDELINGTS, TEMA, jp.tema, FORDELINGSTYPE, INGEN.name,TITTEL, first, KANAL, jp.kanal, BREVKODE, second, EGENANSATT,
                    "${jp.bruker?.erEgenAnsatt ?: false}")
        }

    fun pair(jp: Journalpost): Pair<String,String>  {
        val tittel = fixMeldekort(jp.tittel?.let { if (it.startsWith("Meldekort for uke", ignoreCase = true)) "Meldekort" else it } ?: "Ingen tittel")
        val brevkode = brevkode(jp, tittel)
        return Pair(tittel,brevkode)
    }

    private fun fixMeldekort(tittel: String) = if (tittel.startsWith("korrigert meldekort", ignoreCase = true)) "Korrigert meldekort" else tittel

    private fun brevkode(jp: Journalpost, tittel: String) =
        if (jp.hovedDokumentBrevkode.startsWith("ukjent brevkode", ignoreCase = true) && tittel.contains("meldekort", ignoreCase = true)) "Meldekort"
        else jp.hovedDokumentBrevkode

    private fun JournalfoeringHendelseRecord.tema() = temaNytt.lowercase()

}