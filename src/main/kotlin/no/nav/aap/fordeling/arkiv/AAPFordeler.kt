package no.nav.aap.fordeling.arkiv

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
    override fun fordel(jp: Journalpost) =
        when (val brevkode = jp.dokumenter.first().brevkode) {
            STANDARD.kode -> fordelStandard(jp) // 2c
            STANDARD_ETTERSENDING.kode -> fordelEttersending(jp) // 2d
            else -> FordelingResultat("$brevkode Ikke fordelt").also {
                log.info("$brevkode ikke fordelt")
            }
        }

    private fun fordelStandard(jp: Journalpost) =
        with(integrasjoner) {
            if (!arena.harAktivSak(jp)) { // 2c-1
                val enhet = navEnhet(jp)
                val sak = arena.opprettArenaOppgave(jp,enhet)  // 2c-2
                arkiv.oppdaterOgFerdigstill(jp, sak, enhet) // 3a/b
                FordelingResultat("OK")
            }
            else {
                manuell.fordel(jp)
            }
        }

    private fun fordelEttersending(jp: Journalpost) =
        with(integrasjoner) {
            arena.hentNyesteAktiveSak().let {
                arkiv.oppdaterOgFerdigstill(jp, it, navEnhet(jp)) // 3a/b
                FordelingResultat("Ettersending")
            }
        }

    private fun navEnhet(jp: Journalpost) =
        jp.journalførendeEnhet?.let { enhet ->
            if (integrasjoner.org.erAktiv(enhet))
                NavEnhet(enhet, AKTIV).also { log.info("Journalførende enhet $it er aktiv") }
            else {
                enhetFor(jp).also { log.info("Enhet ikke aktiv fra GT er $it") }
            }
        }?: enhetFor(jp).also { log.info("Enhet ikke satt, fra GT er den $it") }

    private fun enhetFor(jp: Journalpost) =
        with(integrasjoner)  {
            org.navEnhet(pdl.geoTilknytning(jp.fnr), egen.erSkjermet(jp.fnr), pdl.diskresjonskode(jp.fnr))
        }
}