package no.nav.aap.fordeling.arkiv.fordeling

import org.springframework.stereotype.Component
import no.nav.aap.api.felles.SkjemaType.STANDARD
import no.nav.aap.api.felles.SkjemaType.STANDARD_ETTERSENDING
import no.nav.aap.fordeling.arena.ArenaClient
import no.nav.aap.fordeling.arkiv.ArkivClient
import no.nav.aap.fordeling.arkiv.fordeling.Fordeler.FordelerConfig
import no.nav.aap.fordeling.arkiv.fordeling.Fordeler.FordelerConfig.Companion.DEV_AAP
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.FordelingResultat
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.FordelingResultat.FordelingType.AUTOMATISK
import no.nav.aap.fordeling.navenhet.EnhetsKriteria.NavOrg.NAVEnhet
import no.nav.aap.util.LoggerUtil.getLogger
import no.nav.boot.conditionals.Cluster.Companion.currentCluster

@Component
class AAPFordeler(private val arena : ArenaClient, private val arkiv : ArkivClient, protected val manuell : ManuellFordelingFactory,
                  override val cfg : FordelerConfig = DEV_AAP) : Fordeler {

    private val log = getLogger(AAPFordeler::class.java)

    override fun fordelManuelt(jp : Journalpost, enhet : NAVEnhet?) = manuell.fordel(jp, enhet)

    override fun fordel(jp : Journalpost, enhet : NAVEnhet?) =
        enhet?.let { e ->
            runCatching {
                when (jp.hovedDokumentBrevkode) {

                    STANDARD.kode -> {
                        log.info("Automatisk journalføring av ${jp.journalpostId} med brevkode '${jp.hovedDokumentBrevkode}' i cluster $currentCluster")
                        fordelSoknad(jp, e)
                    }

                    STANDARD_ETTERSENDING.kode -> {
                        log.info("Automatisk journalføring av ${jp.journalpostId} med brevkode '${jp.hovedDokumentBrevkode}' i cluster $currentCluster")
                        fordelEttersending(jp, e)
                    }

                    else -> {
                        log.info("Automatisk journalføring av ${jp.journalpostId} med brevkode '${jp.hovedDokumentBrevkode}' ikke konfigurert i cluster $currentCluster, gjør manuell fordeling")
                        manuell.fordel(jp, e)
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
        } ?: manuell.fordel(jp)

    private fun fordelSoknad(jp : Journalpost, enhet : NAVEnhet) =
        if (!arena.harAktivSak(jp.fnr)) {
            log.info("Arena har IKKE aktiv sak for ${jp.fnr}, oppretter oppgave i Arena, pppdaterer og ferdigstiller journalpost ${jp.journalpostId}")
            opprettArenaOppgave(jp, enhet)
            FordelingResultat(AUTOMATISK, "Vellykket fordeling av ${jp.hovedDokumentBrevkode}", jp.hovedDokumentBrevkode, jp.journalpostId)
        }
        else {
            log.info("Arena HAR aktiv sak for ${jp.fnr}, oppretter ikke oppgave i Arena, sender til manuell fordeling")
            manuell.fordel(jp, enhet)
        }

    protected fun opprettArenaOppgave(jp : Journalpost, enhet : NAVEnhet) {
        log.info("Oppretter Arena oppgave for journalpost ${jp.journalpostId}")
        arena.opprettOppgave(jp, enhet).run {
            arkiv.oppdaterOgFerdigstillJournalpost(jp, arenaSakId)
        }
        log.info("Opprettet Arena oppgave for journalpost ${jp.journalpostId}")
    }

    private fun fordelEttersending(jp : Journalpost, enhet : NAVEnhet) =
        arena.nyesteAktiveSak(jp.fnr)?.run {
            ferdigstillEttersending(jp, this)
            FordelingResultat(AUTOMATISK, "Vellykket fordeling av ettersending  ${jp.hovedDokumentBrevkode}", jp.hovedDokumentBrevkode, jp.journalpostId)
        } ?: run {
            log.warn("Arena har IKKE aktiv sak for ettersending ${jp.fnr}, kan ikke oppdatere og ferdigstille journalpost, sender til manuell fordeling")
            manuell.fordel(jp, enhet)
        }

    protected fun ferdigstillEttersending(jp : Journalpost, nyesteSak : String) {
        log.info("Oppdaterer og ferdigstiller ettersending for journalpost ${jp.journalpostId}")
        arkiv.oppdaterOgFerdigstillJournalpost(jp, nyesteSak)
        log.info("Oppdaterert og ferdigstilt ettersending for journalpost ${jp.journalpostId}")
    }

    override fun toString() = "AAPFordeler(arena=$arena, arkiv=$arkiv, cfg=$cfg, manuelle=$manuell)"
}