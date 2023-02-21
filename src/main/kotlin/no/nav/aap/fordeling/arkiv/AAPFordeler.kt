package no.nav.aap.fordeling.arkiv

import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.felles.SkjemaType.*
import no.nav.aap.fordeling.arkiv.Fordeler.FordelingResultat
import no.nav.aap.fordeling.navorganisasjon.EnhetsKriteria.Status.AKTIV
import no.nav.aap.fordeling.navorganisasjon.NavEnhet
import no.nav.aap.util.Constants.AAP
import no.nav.aap.util.LoggerUtil.getLogger
import org.springframework.stereotype.Component

@Component
class AAPFordeler(private val integrasjoner: Integrasjoner,private val manuell: AAPManuellFordeler) : Fordeler {

    private val log = getLogger(javaClass)
    override fun tema() = listOf(AAP)
    override fun fordel(journalpost: Journalpost) =
        runCatching {
            when (val brevkode = journalpost.dokumenter.first().brevkode) {
                STANDARD.kode -> fordelStandard(journalpost) // 2c
                STANDARD_ETTERSENDING.kode -> fordelEttersending(journalpost) // 2d
                else -> FordelingResultat(msg="$brevkode ikke konfigurert for fordeling for ${tema()}").also {
                    log.info("$brevkode ikke konfigurert for fordeling for ${tema()}")
                }
            }
        }.getOrElse {
            log.warn("Noe gikk galt under fordeling, går til manuell",it)
            manuell.fordel(journalpost)
        }



    private fun fordelStandard(journalpost: Journalpost) =
        with(integrasjoner) {
            if (!arena.harAktivArenaSak(journalpost)) { // 2c-1
                arena.opprettArenaOppgave(journalpost, navEnhet(journalpost)).run {
                    arkiv.oppdaterOgFerdigstillJournalpost(journalpost, arenaSakId) // 3a/b
                }  // 2c-2
            }
            else {
                manuell.fordel(journalpost)
            }
        }

    private fun fordelEttersending(journalpost: Journalpost) =
        with(integrasjoner) {
            arena.nyesteAktiveArenaSak(journalpost)?.run {
                arkiv.oppdaterOgFerdigstillJournalpost(journalpost, this) // 3a/b
            } ?: manuell.fordel(journalpost)
        }

    private fun navEnhet(journalpost: Journalpost) =
        with(journalpost) {
            journalførendeEnhet?.let { enhet ->
                if (integrasjoner.org.erAktiv(enhet))
                    NavEnhet(enhet, AKTIV).also { log.info("Journalførende enhet $it er aktiv") }
                else {
                    enhetFor(fnr).also { log.info("Enhet ikke aktiv fra GT er $it") }
                }
            }?: enhetFor(fnr).also { log.info("Enhet ikke satt, fra GT er den $it") }
        }


    private fun enhetFor(fnr: Fødselsnummer) =
        with(integrasjoner)  {
            org.navEnhet(pdl.geoTilknytning(fnr), egen.erSkjermet(fnr), pdl.diskresjonskode(fnr))
        }
}