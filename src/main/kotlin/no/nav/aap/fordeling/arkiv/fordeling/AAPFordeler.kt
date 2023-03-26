package no.nav.aap.fordeling.arkiv.fordeling

import no.nav.aap.api.felles.SkjemaType.STANDARD
import no.nav.aap.api.felles.SkjemaType.STANDARD_ETTERSENDING
import no.nav.aap.fordeling.arena.ArenaClient
import no.nav.aap.fordeling.arkiv.ArkivClient
import no.nav.aap.fordeling.arkiv.fordeling.FordelerConfig.Companion.DEV_AAP
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.FordelingResultat
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.FordelingResultat.FordelingType.AUTOMATISK
import no.nav.aap.fordeling.navenhet.EnhetsKriteria.NavOrg.NAVEnhet
import no.nav.aap.util.LoggerUtil.getLogger
import org.springframework.stereotype.Component

@Component
class AAPFordeler(
        private val arena: ArenaClient,
        private val arkiv: ArkivClient,
        protected val  manuell: ManuellFordelingFactory) : Fordeler {

    private val log = getLogger(AAPFordeler::class.java)
    override  val cfg = DEV_AAP  // For NOW
    override fun fordelManuelt(jp: Journalpost, enhet: NAVEnhet?) = manuell.fordel(jp,enhet)
    override fun fordel(jp: Journalpost, enhet: NAVEnhet?)  =
        enhet?.let {e ->
            runCatching {
                when (jp.hovedDokumentBrevkode) {

                    STANDARD.kode -> {
                        log.info("Automatisk journalføring av ${jp.journalpostId} med brevkode '${jp.hovedDokumentBrevkode}'")
                        fordelStandard(jp, e)
                    }

                    STANDARD_ETTERSENDING.kode -> {
                        log.info("Automatisk journalføring av ${jp.journalpostId} med brevkode '${jp.hovedDokumentBrevkode}'")
                        fordelEttersending(jp,e)
                    }

                    else -> {
                        log.info("Automatisk journalføring av ${jp.journalpostId} med brevkode '${jp.hovedDokumentBrevkode}' ikke konfigurert, gjør manuell fordeling")
                        manuell.fordel(jp,e)
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
            log.info("Arena har IKKE aktiv sak for ${jp.fnr}, oppretter oppgave i Arena, ppdaterer og ferdigstiller journalpost ${jp.journalpostId}")
            ferdigstillStandard(jp,enhet)
            FordelingResultat(AUTOMATISK, "Vellykket fordeling av ${jp.hovedDokumentBrevkode}", jp.hovedDokumentBrevkode, jp.journalpostId)
        }
        else {
            log.info("Arena HAR aktiv sak for ${jp.fnr}, oppretter ikke oppgave i Arena, sender til manuell fordeling")
            manuell.fordel(jp,enhet)
        }

    protected fun ferdigstillStandard(jp: Journalpost, enhet: NAVEnhet) {
        arena.opprettOppgave(jp, enhet).run {
            arkiv.oppdaterOgFerdigstillJournalpost(jp, arenaSakId)
        }
    }
    private fun fordelEttersending(jp: Journalpost,enhet: NAVEnhet) =
        arena.nyesteAktiveSak(jp.fnr)?.run {
            ferdigstillEttersending(jp,this)
            FordelingResultat(AUTOMATISK, "Vellykket fordeling av ettersending  ${jp.hovedDokumentBrevkode}", jp.hovedDokumentBrevkode, jp.journalpostId)
        } ?: run {
            log.warn("Arena har IKKE aktiv sak for ${jp.fnr}, kan ikke oppdatere og ferdigstille journalpost, sender til manuell fordeling")
            manuell.fordel(jp,enhet)
        }


    protected fun ferdigstillEttersending(jp: Journalpost, nyesteSak: String) {
        arkiv.oppdaterOgFerdigstillJournalpost(jp, nyesteSak)
    }

    override fun toString(): String {
        return "AAPFordeler(arena=$arena, arkiv=$arkiv, cfg=$cfg, manuelle=$manuell)"
    }
}