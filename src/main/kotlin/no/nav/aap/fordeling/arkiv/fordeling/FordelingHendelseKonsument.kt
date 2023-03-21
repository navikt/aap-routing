package no.nav.aap.fordeling.arkiv.fordeling

import no.nav.aap.api.felles.error.IrrecoverableIntegrationException
import no.nav.aap.api.felles.error.RecoverableIntegrationException
import no.nav.aap.fordeling.arkiv.ArkivClient
import no.nav.aap.fordeling.arkiv.fordeling.FordelingConfig.Companion.FORDELING
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.FordelingResultat.FordelingType.DIREKTE_MANUELL
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.FordelingResultat.FordelingType.INGEN
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.JournalpostDTO.JournalStatus.MOTTATT
import no.nav.aap.fordeling.egenansatt.EgenAnsattClient
import no.nav.aap.fordeling.navenhet.NavEnhetUtvelger
import no.nav.aap.fordeling.person.PDLClient
import no.nav.aap.fordeling.slack.Slacker
import no.nav.aap.util.ChaosMonkey
import no.nav.aap.util.LoggerUtil.getLogger
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
@ConditionalOnGCP
class FordelingHendelseKonsument(
        private val factory: FordelingFactory,
        private val arkiv: ArkivClient,
        private val enhet: NavEnhetUtvelger,
        private val slack: Slacker,
        private val pdl: PDLClient, //TODO Midlertidig
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
            monkey.injectFault("FordelingHendelseKonsument",RecoverableIntegrationException("Chaos Monkey recoverable exception"))
            log.info("Mottatt journalpost ${h.journalpostId} med tema ${h.tema()} på $topic for ${n?.let { "$it." } ?: "1."} gang.")
            val jp = arkiv.hentJournalpost("${h.journalpostId}")

            if (jp == null)  {
                log.warn("Ingen journalpost, lar dette fanges opp av sikkerhetsnettet")
                return
            }

            if (jp.bruker == null) {
                log.warn("Ingen bruker er satt på journalposten, går direkte til manuell journalføring (snart)")
                jp.metrikker(DIREKTE_MANUELL,topic)
               // return fordeler.fordelManuelt(jp, FORDELINGSENHET)
                return
            }

            if (isProd()) {
                jp.metrikker(INGEN,topic)
                monkey.injectFault("FordelingHendelseKonsument",IrrecoverableIntegrationException("Chaos Monkey irrecoverable exception"))
                pdl.geoTilknytning(jp.fnr)  // Resilience test web client
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

    private fun JournalfoeringHendelseRecord.tema() = temaNytt.lowercase()

}