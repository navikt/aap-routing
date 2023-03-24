package no.nav.aap.fordeling.arkiv.fordeling

import no.nav.aap.api.felles.SkjemaType.STANDARD
import no.nav.aap.api.felles.SkjemaType.STANDARD_ETTERSENDING
import no.nav.aap.fordeling.arena.ArenaClient
import no.nav.aap.fordeling.arkiv.ArkivClient
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.FordelingResultat
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.FordelingResultat.FordelingType.AUTOMATISK
import no.nav.aap.fordeling.navenhet.EnhetsKriteria.NavOrg.NAVEnhet
import no.nav.aap.util.Constants.AAP
import no.nav.aap.util.LoggerUtil.getLogger
import no.nav.boot.conditionals.Cluster.Companion.devClusters
import org.springframework.stereotype.Component

@Component
class AAPFordeler(
        private val arena: ArenaClient,
        private val arkiv: ArkivClient,
        protected val  manuell: ManuellFordelingFactory) : Fordeler {

    private val log = getLogger(AAPFordeler::class.java)
    override fun clusters() = devClusters()  // For NOW
    override fun tema() = listOf(AAP)
    override fun fordelManuelt(jp: Journalpost, enhet: NAVEnhet?) = manuell.fordel(jp,enhet)
    override fun fordel(jp: Journalpost, enhet: NAVEnhet?): FordelingResultat =
        enhet?.let {e ->
            runCatching {
                when (jp.hovedDokumentBrevkode) {
                    STANDARD.kode -> {
                        log.info("Forsøker automatisk journalføring av ${jp.journalpostId} med brevkode ${jp.hovedDokumentBrevkode}")
                        fordelStandard(jp, e)
                    }

                    STANDARD_ETTERSENDING.kode -> {
                        log.info("Forsøker automatisk journalføring av ${jp.journalpostId} med brevkode ${jp.hovedDokumentBrevkode}")
                        fordelEttersending(jp)
                    }

                    else -> {
                        log.info("Brevkode ${jp.hovedDokumentBrevkode} ikke konfigurert for automatisk fordeling for ${tema()}, forsøker manuelt")
                        manuell.fordel(jp,enhet)
                    }
                }
            }.getOrElse {
                if (it !is ManuellFordelingException) {
                    log.warn("Kunne ikke automatisk fordele journalpost ${jp.journalpostId} (${jp.hovedDokumentBrevkode}), forsøker manuelt", it)
                    manuell.fordel(jp, e)
                }
                else {
                    log.info("Gjør ikke umiddelbart nytt forsøk på manuelt oppave siden manuelt forsøk akkurat feilet (${it.message})", it)
                    throw it
                }
            }
        } ?:  manuell.fordel(jp)


    private fun fordelStandard(jp: Journalpost, enhet: NAVEnhet) =
        if (!arena.harAktivSak(jp.fnr)) {
            log.info("Arena har IKKE aktiv sak for ${jp.fnr}")
            ferdigstillStandard(jp,enhet)
            FordelingResultat(AUTOMATISK, "Vellykket fordeling av ${jp.hovedDokumentBrevkode}", jp.hovedDokumentBrevkode, jp.journalpostId)
        }
        else {
            with("Har aktiv sak for ${jp.fnr}, skal IKKE opprett oppgave i Arena") {
                log.info(this)
                throw ArenaSakException(this)
            }
         }

    protected fun ferdigstillStandard(jp: Journalpost, enhet: NAVEnhet) {
        arena.opprettOppgave(jp, enhet).run {
            arkiv.oppdaterOgFerdigstillJournalpost(jp, arenaSakId)
        }
    }
    private fun fordelEttersending(jp: Journalpost) =
        arena.nyesteAktiveSak(jp.fnr)?.run {
            ferdigstillEttersending(jp,this)
            FordelingResultat(AUTOMATISK, "Vellykket fordeling", jp.hovedDokumentBrevkode, jp.journalpostId)
        } ?: throw ArenaSakException("Arena har IKKE aktiv sak for ${jp.fnr}, kan ikke oppdatere og ferdigstille journalpost").also {
            log.warn(it.message,it)
        }

    protected fun ferdigstillEttersending(jp: Journalpost, nyesteSak: String) {
        arkiv.oppdaterOgFerdigstillJournalpost(jp, nyesteSak)
    }

    override fun toString(): String {
        return "AAPFordeler(arena=$arena, arkiv=$arkiv, tema=${tema()}, clusters=${clusters().asList()}, manuelle=$manuell)"
    }
}