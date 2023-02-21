package no.nav.aap.fordeling.arkiv

import no.nav.aap.api.felles.SkjemaType.*
import no.nav.aap.fordeling.arena.ArenaDTOs.ArenaOpprettetOppgave.Companion.TIL_MANUELL
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
        when (val brevkode = journalpost.dokumenter.first().brevkode) {
            STANDARD.kode -> fordelStandard(journalpost) // 2c
            STANDARD_ETTERSENDING.kode -> fordelEttersending(journalpost) // 2d
            else -> FordelingResultat("$brevkode Ikke fordelt").also {
                log.info("$brevkode ikke fordelt")
            }
        }

    private fun fordelStandard(journalpost: Journalpost) =
        with(integrasjoner) {
            if (!arena.harAktivSak(journalpost)) { // 2c-1
                val enhet = navEnhet(journalpost)
                val sak = arena.opprettArenaOppgave(journalpost,enhet)  // 2c-2
                if (TIL_MANUELL == sak) {
                    return manuell.fordel(journalpost)
                }
                arkiv.oppdaterOgFerdigstill(journalpost, sak) // 3a/b
                FordelingResultat("OK")
            }
            else {
                manuell.fordel(journalpost)
            }
        }

    private fun fordelEttersending(journalpost: Journalpost) =
        with(integrasjoner) {
            arena.hentNyesteAktiveSak().let {
                arkiv.oppdaterOgFerdigstill(journalpost, it) // 3a/b
                FordelingResultat("Ettersending")
            }
        }

    private fun navEnhet(journalpost: Journalpost) =
        with(journalpost) {
            journalførendeEnhet?.let { enhet ->
                if (integrasjoner.org.erAktiv(enhet))
                    NavEnhet(enhet, AKTIV).also { log.info("Journalførende enhet $it er aktiv") }
                else {
                    enhetFor(this).also { log.info("Enhet ikke aktiv fra GT er $it") }
                }
            }?: enhetFor(this).also { log.info("Enhet ikke satt, fra GT er den $it") }
        }


    private fun enhetFor(journalpost: Journalpost) =
        with(integrasjoner)  {
            org.navEnhet(pdl.geoTilknytning(journalpost.fnr), egen.erSkjermet(journalpost.fnr), pdl.diskresjonskode(journalpost.fnr))
        }
}