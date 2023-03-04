package no.nav.aap.fordeling.arkiv.fordeling

import no.nav.aap.api.felles.SkjemaType.STANDARD
import no.nav.aap.api.felles.SkjemaType.STANDARD_ETTERSENDING
import no.nav.aap.fordeling.Integrasjoner
import no.nav.aap.fordeling.arkiv.fordeling.Fordeler.FordelingResultat
import no.nav.aap.fordeling.arkiv.fordeling.Fordeler.FordelingResultat.FordelingType.AUTOMATISK
import no.nav.aap.fordeling.navorganisasjon.EnhetsKriteria.NAVEnhet
import no.nav.aap.util.Constants.AAP
import no.nav.aap.util.LoggerUtil.getLogger
import org.springframework.stereotype.Component

@Component
class AAPFordeler(private val integrasjoner: Integrasjoner, private val manuell: AAPManuellFordeler) : Fordeler {

    private val log = getLogger(javaClass)
    override fun tema() = listOf(AAP)
    override fun fordel(jp: Journalpost, enhet: NAVEnhet) =
        runCatching {
            when (jp.hovedDokumentBrevkode) {
                STANDARD.kode -> {
                    log.info("Forsøker automatisk journalføring av ${jp.journalpostId} med brevkode ${jp.hovedDokumentBrevkode}")
                    fordelStandard(jp,enhet)
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
            if (it !is ManuellException) {
                log.warn("Kunne ikke automatisk fordele journalpost ${jp.journalpostId} (${jp.hovedDokumentBrevkode}), forsøker manuelt",it)
                manuell.fordel(jp,enhet)
            }
            else {
                log.info("Gjør ikke umiddebart nytt forsøk på manuelt oppave siden manuelt forsøk akkurat feilet",it)
                throw it
            }
        }

    private fun fordelStandard(jp: Journalpost, enhet: NAVEnhet) =
        with(integrasjoner) {
            if (!arena.harAktivSak(jp.fnr)) {
                log.info("Arena har IKKE aktiv sak for ${jp.fnr}")
                arena.opprettOppgave(jp, enhet).run {
                    arkiv.oppdaterOgFerdigstillJournalpost(jp, arenaSakId)
                    FordelingResultat(jp.journalpostId, "Vellykket fordeling av ${jp.hovedDokumentBrevkode}",AUTOMATISK)
                }
            }
            else {
                log.warn("Arena HAR aktiv sak for ${jp.fnr}")
                throw ArenaSakException(jp.journalpostId,"Har aktiv sak for ${jp.fnr}, kan ikke opprett oppgave i Arena")
            }
        }

    private fun fordelEttersending(jp: Journalpost) =
        with(integrasjoner) {
            arena.nyesteAktiveSak(jp.fnr)?.run {
                arkiv.oppdaterOgFerdigstillJournalpost(jp, this) // 3a/b
                FordelingResultat(jp.journalpostId, "Vellykket fordeling av ${jp.hovedDokumentBrevkode}",AUTOMATISK)
            } ?: throw ArenaSakException(jp.journalpostId,"Har IKKE aktiv sak for ${jp.fnr}, kan ikke oppdatere og ferdigstille journalpost")
        }
}

class ArenaSakException(val journalpostId: String, msg: String) : RuntimeException(msg)