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
    override fun fordel(journalpost: Journalpost, enhet: NavEnhet) =
        runCatching {
            when (val brevkode = journalpost.hovedDokumentBrevkode) {
                STANDARD.kode -> fordelStandard(journalpost,enhet)
                STANDARD_ETTERSENDING.kode -> fordelEttersending(journalpost,enhet)
                else -> FordelingResultat(msg="Brevkode $brevkode ikke konfigurert for fordeling for ${tema()}").also {
                    log.info("Brevkode $brevkode ikke konfigurert for fordeling for ${tema()}")
                }
            }
        }.getOrElse {
            runCatching {
                log.warn("Kunne ikke automatisk fordele, prøver manuell",it)
                manuell.fordel(journalpost,enhet)
            }.getOrElse {e ->
                log.warn("Noe gikk galt under manuell fordeling",e)
                throw e
            }
        }

    private fun fordelStandard(journalpost: Journalpost, enhet: NavEnhet) =
        with(integrasjoner) {
            if (!arena.harAktivSak(journalpost)) {
                arena.opprettOppgave(journalpost, enhet).run {
                    arkiv.oppdaterOgFerdigstillJournalpost(journalpost, arenaSakId)
                }
            }
            else {
                throw ArenaAktivSakException("Journalpost ${journalpost.journalpostId} har aktiv sak for ${journalpost.fnr}, kan ikke opprett sak i Arena")
            }
        }

    private fun fordelEttersending(journalpost: Journalpost, enhet: NavEnhet) =
        with(integrasjoner) {
            arena.nyesteAktiveSak(journalpost)?.run {
                arkiv.oppdaterOgFerdigstillJournalpost(journalpost, this) // 3a/b
            } ?: manuell.fordel(journalpost,enhet)
        }
}

class ArenaAktivSakException(msg: String) : RuntimeException(msg)