package no.nav.aap.fordeling.arkiv.fordeling

import org.springframework.stereotype.Component
import no.nav.aap.api.felles.SkjemaType.STANDARD
import no.nav.aap.api.felles.SkjemaType.STANDARD_ETTERSENDING
import no.nav.aap.fordeling.arena.ArenaClient
import no.nav.aap.fordeling.arkiv.ArkivClient
import no.nav.aap.fordeling.arkiv.fordeling.Fordeler.FordelingResultat
import no.nav.aap.fordeling.arkiv.fordeling.Fordeler.FordelingResultat.FordelingType.AUTOMATISK
import no.nav.aap.fordeling.navenhet.NAVEnhet
import no.nav.aap.util.LoggerUtil.getLogger
import no.nav.boot.conditionals.Cluster.Companion.isProd

@Component
class AAPFordeler(private val arena : ArenaClient, private val arkiv : ArkivClient, protected val manuell : AAPManuellFordeler) :
    Fordeler {

    private val log = getLogger(AAPFordeler::class.java)

    override fun fordelManuelt(jp : Journalpost, enhet : NAVEnhet?) = manuell.fordel(jp, enhet)

    override fun fordel(jp : Journalpost, enhet : NAVEnhet?) =
        enhet?.let { e ->
            runCatching {
                when (jp.hovedDokumentBrevkode) {

                    STANDARD.kode -> {
                        log.info("Automatisk journalføring av journalpost ${jp.id} med brevkode '${jp.hovedDokumentBrevkode}'")
                        fordelSøknad(jp, e)
                    }

                    STANDARD_ETTERSENDING.kode -> {
                        log.info("Automatisk journalføring av journalpost ${jp.id} med brevkode '${jp.hovedDokumentBrevkode}'")
                        fordelEttersending(jp, e)
                    }

                    else -> {
                        log.info("Automatisk journalføring av journalpost ${jp.id} med brevkode '${jp.hovedDokumentBrevkode}' ikke konfigurert, gjør manuell fjournalføring/ordeling")
                        manuell.fordel(jp, e)
                    }
                }
            }.getOrElse {
                if (it !is ManuellFordelingException) {
                    log.warn("Kunne ikke automatisk fordele journalpost ${jp.id} (${jp.hovedDokumentBrevkode}), forsøker manuelt", it)
                    manuell.fordel(jp, e)
                }
                else {
                    log.info("Gjør ikke umiddelbart nytt forsøk på manuelt oppave siden manuelt forsøk akkurat feilet (${it.message})", it)
                    throw it
                }
            }
        } ?: manuell.fordel(jp)

    private fun fordelSøknad(jp : Journalpost, enhet : NAVEnhet) =
        if (!arena.harAktivSak(jp.fnr)) {
            log.info("Arena har IKKE aktiv sak, oppretter oppgave i Arena, 0ppdaterer og ferdigstiller journalpost ${jp.id}")
            opprettArenaOppgaveOgFerdigstill(jp, enhet)
            FordelingResultat(AUTOMATISK, "Vellykket fordeling av ${jp.hovedDokumentBrevkode}", jp.hovedDokumentBrevkode, jp.id)
        }
        else {
            log.info("Arena HAR aktiv sak for ${jp.fnr}, oppretter ikke oppgave i Arena, sender til manuell fordeling")
            manuell.fordel(jp, enhet)
        }

    protected fun opprettArenaOppgaveOgFerdigstill(jp : Journalpost, enhet : NAVEnhet) {
        if (isProd()) {
            log.info("Oppretter ingen Arena oppgave for journalpost ${jp.id} i prod foreløpig")
        }
        else {
            log.info("Oppretter Arena oppgave for journalpost ${jp.id}")
            arena.opprettOppgave(jp, enhet).run {
                arkiv.oppdaterOgFerdigstillJournalpost(jp, arenaSakId)
            }
            log.info("Opprettet Arena oppgave for journalpost ${jp.id}")
        }
    }

    private fun fordelEttersending(jp : Journalpost, enhet : NAVEnhet) =
        arena.nyesteAktiveSak(jp.fnr)?.run {
            ferdigstillEttersending(jp, this)
            FordelingResultat(AUTOMATISK, "Vellykket fordeling av ettersending  ${jp.hovedDokumentBrevkode}", jp.hovedDokumentBrevkode, jp.id)
        } ?: run {
            log.warn("Arena har IKKE aktiv sak for ettersending ${jp.id}, kan ikke oppdatere og ferdigstille journalpost, sender til manuell fordeling")
            manuell.fordel(jp, enhet)
        }

    protected fun ferdigstillEttersending(jp : Journalpost, nyesteSak : String) {
        if (isProd()) {
            log.info("Ingen Oppdaterting og ferdigstilling av ettersending for journalpost ${jp.id} i prod")
        }
        else {
            log.info("Oppdaterer og ferdigstiller ettersending for journalpost ${jp.id}")
            arkiv.oppdaterOgFerdigstillJournalpost(jp, nyesteSak)
            log.info("Oppdaterert og ferdigstilt ettersending for journalpost ${jp.id}")
        }
    }

    override fun toString() = "AAPFordeler(arena=$arena, arkiv=$arkiv,manuelle=$manuell)"
}