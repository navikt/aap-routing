package no.nav.aap.fordeling.arkiv.fordeling

import io.micrometer.observation.annotation.Observed
import no.nav.aap.api.felles.error.IrrecoverableIntegrationException
import no.nav.aap.fordeling.arkiv.ArkivClient
import no.nav.aap.fordeling.arkiv.fordeling.FordelingConfig.Companion.FORDELING
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.FordelingResultat.FordelingType.DIREKTE_MANUELL
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.FordelingResultat.FordelingType.INGEN
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.FordelingResultat.FordelingType.INGEN_JOURNALPOST
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.JournalpostDTO.JournalStatus.MOTTATT
import no.nav.aap.fordeling.navenhet.EnhetsKriteria.NavOrg.NAVEnhet.Companion.FORDELINGSENHET
import no.nav.aap.fordeling.navenhet.NavEnhetUtvelger
import no.nav.aap.fordeling.slack.Slacker
import no.nav.aap.fordeling.util.MetrikkLabels.FORDELINGSTYPE
import no.nav.aap.fordeling.util.MetrikkLabels.FORDELINGTS
import no.nav.aap.fordeling.util.MetrikkLabels.KANAL
import no.nav.aap.util.LoggerUtil.getLogger
import no.nav.aap.util.Metrikker
import no.nav.aap.util.Metrikker.inc
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
        private val slack: Slacker) {

    val log = getLogger(FordelingHendelseKonsument::class.java)

    @KafkaListener(topics = ["#{'\${fordeling.topics.main}'}"], containerFactory = FORDELING)
    @RetryableTopic(attempts = "#{'\${fordeling.topics.retries}'}", backoff = Backoff(delayExpression = "#{'\${fordeling.topics.backoff}'}"),
            sameIntervalTopicReuseStrategy = SINGLE_TOPIC,
            exclude = [IrrecoverableIntegrationException::class],
            autoStartDltHandler = "true",
            autoCreateTopics = "false")
    fun listen(h: JournalfoeringHendelseRecord, @Header(DEFAULT_HEADER_ATTEMPTS, required = false) n: Int?, @Header(RECEIVED_TOPIC) topic: String) {
        runCatching {

           val fordeler =  factory.fordelerFor(h.tema())
            log.info("Mottatt journalpost ${h.journalpostId} med tema ${h.tema()} på $topic for ${n?.let { "$it." } ?: "1."} gang.")
            val jp = arkiv.hentJournalpost("${h.journalpostId}")

            if (jp == null)  {
                log.warn("Ingen journalpost, lar dette fanges opp av sikkerhetsnettet")
                inc(FORDELINGTS, TOPIC,topic,KANAL,h.mottaksKanal,FORDELINGSTYPE, INGEN_JOURNALPOST.name)
                return
            }

            if (jp.bruker == null) {
                log.warn("Ingen bruker er satt på journalposten, går direkte til manuell journalføring")
                fordeler.fordelManuelt(jp, FORDELINGSENHET)
                jp.metrikker(DIREKTE_MANUELL,topic)
                return
            }

            jp.run {
                if (factory.isEnabled()) {  // TODO en MOTTATT sjekk, kanskje ?
                   log.info("Fordeler $journalpostId med brevkode $hovedDokumentBrevkode")
                    factory.fordelerFor(h.tema()).fordel(this, enhet.navEnhet(this)).also {
                        with("${it.msg()} ($fnr)") {
                            log.info(this)
                            slack.jippiHvisDev(this)
                            metrikker(it.fordelingstype,topic)
                        }
                    }
                }
                else {
                    log.info("Ingen fordeling av $journalpostId, sett 'fordeling.enabled=true' for å aktivere")
                }
            }
        }.onFailure {
            with("Fordeling av journalpost ${h.journalpostId} feilet for ${n?.let { "$it." } ?: "1."} gang på topic $topic") {
                log.warn("$this (${it.javaClass.simpleName})", it)
                slack.okHvisdev("$this. (${it.message})")
            }
            throw it
        }
    }

    @DltHandler
    fun dlt(h: JournalfoeringHendelseRecord, @Header(EXCEPTION_STACKTRACE) trace: String?) {
        with("Gir opp fordeling av journalpost ${h.journalpostId}") {
            log.warn(this)
            slack.okHvisdev(this)
        }
    }

    private fun JournalfoeringHendelseRecord.tema() = temaNytt.lowercase()

}