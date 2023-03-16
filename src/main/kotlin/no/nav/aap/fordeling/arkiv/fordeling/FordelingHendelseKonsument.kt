package no.nav.aap.fordeling.arkiv.fordeling

import no.nav.aap.fordeling.arkiv.ArkivClient
import no.nav.aap.fordeling.arkiv.fordeling.FordelingConfig.Companion.FORDELING
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.JournalpostDTO.JournalStatus.MOTTATT
import no.nav.aap.fordeling.config.GlobalBeanConfig.FaultInjecter
import no.nav.aap.fordeling.config.Metrikker
import no.nav.aap.fordeling.config.Metrikker.Companion.BREVKODE
import no.nav.aap.fordeling.config.Metrikker.Companion.KANAL
import no.nav.aap.fordeling.config.Metrikker.Companion.TITTEL
import no.nav.aap.fordeling.navenhet.NavEnhetUtvelger
import no.nav.aap.fordeling.slack.Slacker
import no.nav.aap.util.Constants.TEMA
import no.nav.aap.util.LoggerUtil.getLogger
import no.nav.boot.conditionals.Cluster.Companion.currentCluster
import no.nav.boot.conditionals.ConditionalOnGCP
import no.nav.boot.conditionals.EnvUtil
import no.nav.boot.conditionals.EnvUtil.isProd
import no.nav.joarkjournalfoeringhendelser.JournalfoeringHendelseRecord
import org.springframework.core.env.Environment
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
        private val fordeler: FordelingTemaDelegator,
        private val arkiv: ArkivClient,
        private val enhet: NavEnhetUtvelger,
        private val slack: Slacker,
        private val faultInjecter: FaultInjecter,
        private val metrikker: Metrikker,
        private val env: Environment) {

    val log = getLogger(FordelingHendelseKonsument::class.java)

    @KafkaListener(topics = ["#{'\${fordeling.topics.main}'}"], containerFactory = FORDELING)
    @RetryableTopic(attempts = "#{'\${fordeling.topics.retries}'}",
            backoff = Backoff(delayExpression = "#{'\${fordeling.topics.backoff}'}"),
            sameIntervalTopicReuseStrategy = SINGLE_TOPIC,
            autoCreateTopics = "false")
    fun listen(
            hendelse: JournalfoeringHendelseRecord,
            @Header(DEFAULT_HEADER_ATTEMPTS, required = false) n: Int?,
            @Header(RECEIVED_TOPIC) topic: String) {
        lateinit var jp: Journalpost
        runCatching {
            log.info("Fordeler journalpost ${hendelse.journalpostId} mottatt på $topic for ${n?.let { "$it." } ?: "1."} gang.")
            faultInjecter.randomFeilHvisDev(this)
            jp = arkiv.hentJournalpost("${hendelse.journalpostId}")
            lagMetrikker(jp)
            if (isProd(env)) {
                log.info("return etter Journalpost $jp")
                return  // TODO Midlertidig
            }
            jp.run {
                if (fordeler.isEnabled() && jp.status == MOTTATT) {
                    fordeler.fordel(this, enhet.navEnhet(this)).run {
                        with("${msg()} ($fnr)") {
                            log.info(this)
                            slack.okHvisDev(this)
                        }
                    }
                }
                else {
                    log.info("Ingen fordeling av $jp, set fordeling:enabled;true for aktivering")
                }
            }
        }.onFailure {
            with("Fordeling av journalpost ${hendelse.journalpostId} $jp feilet for ${n?.let { "$it." } ?: "1."} gang") {
                log.warn(this, it)
                slack.feil("$this (cluster: ${currentCluster().name.lowercase()}). (${it.message})")
            }
            throw it
        }
    }

    private fun lagMetrikker(jp: Journalpost) {
        var tittel = jp.tittel?.let { if (it.startsWith("Meldekort for uke", ignoreCase = true)) "Meldekort" else it } ?: "Ingen tittel"
        tittel = if (tittel.startsWith("korrigert meldekort", ignoreCase = true)) "Korrigert meldekort" else tittel
        val brevkode = if (jp.hovedDokumentBrevkode.startsWith("ukjent brevkode", ignoreCase = true) && tittel.contains("meldekort",
                    ignoreCase = true)) "Meldekort"
        else jp.hovedDokumentBrevkode
        metrikker.inc("jper", TEMA, jp.tema, TITTEL, tittel, KANAL, jp.kanal, BREVKODE, brevkode)
    }

    @DltHandler
    fun dlt(payload: JournalfoeringHendelseRecord, @Header(EXCEPTION_STACKTRACE) trace: String?) {
        with("Gir opp fordeling av journalpost ${payload.journalpostId}") {
            log.warn("$this $trace")
            slack.feil(this)
        }
    }
}