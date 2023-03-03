package no.nav.aap.fordeling.arkiv

import no.nav.aap.api.felles.SkjemaType.STANDARD
import no.nav.aap.api.felles.SkjemaType.STANDARD_ETTERSENDING
import no.nav.aap.fordeling.Integrasjoner
import no.nav.aap.fordeling.arkiv.Fordeler.FordelingResultat
import no.nav.aap.fordeling.navorganisasjon.EnhetsKriteria.NavEnhet
import no.nav.aap.util.Constants.AAP
import no.nav.aap.util.LoggerUtil.getLogger
import org.springframework.stereotype.Component

@Component
class AAPFordeler(private val integrasjoner: Integrasjoner, private val manuell: AAPManuellFordeler) : Fordeler {

    private val log = getLogger(javaClass)
    override fun tema() = listOf(AAP)
    override fun fordel(jp: Journalpost, enhet: NavEnhet) =
        runCatching {
            when (jp.hovedDokumentBrevkode) {
                STANDARD.kode -> fordelStandard(jp,enhet)
                STANDARD_ETTERSENDING.kode -> fordelEttersending(jp,enhet)
                else -> {
                    log.trace("Brevkode ${jp.hovedDokumentBrevkode} ikke konfigurert for automatisk fordeling for ${tema()}, fordeler manuelt")
                    manuell.fordel(jp,enhet)
                }
            }
        }.getOrElse {
            runCatching {
                log.warn("Kunne ikke automatisk fordele journalpost ${jp.journalpostId} (${jp.hovedDokumentBrevkode}), prøver manuell",it)
                manuell.fordel(jp,enhet)
            }.getOrElse {e ->
                log.warn("Noe gikk galt under manuell fordeling av journalpost ${jp.journalpostId}",e)
                throw e
            }
        }

    private fun fordelStandard(jp: Journalpost, enhet: NavEnhet) =
        with(integrasjoner) {
            if (!arena.harAktivSak(jp.fnr)) {
                arena.opprettOppgave(jp, enhet).run {
                    arkiv.oppdaterOgFerdigstillJournalpost(jp, arenaSakId)
                }
            }
            else {
                log.warn("Arena har aktiv sak for ${jp.fnr}")
                throw ArenaSakException(jp.journalpostId,"Har aktiv sak for ${jp.fnr}, kan ikke opprett oppgave i Arena")
            }
        }

    private fun fordelEttersending(jp: Journalpost, enhet: NavEnhet) =
        with(integrasjoner) {
            arena.nyesteAktiveSak(jp.fnr)?.run {
                arkiv.oppdaterOgFerdigstillJournalpost(jp, this) // 3a/b
            } ?: throw ArenaSakException(jp.journalpostId,"Har IKKE aktiv sak for ${jp.fnr}, kan ikke oppdatere og ferdigstille journalpost")
        }
}

class ArenaSakException(val journalpostId: String, msg: String) : RuntimeException(msg)