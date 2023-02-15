package no.nav.aap.fordeling.arkiv

import no.nav.aap.api.felles.SkjemaType.*
import no.nav.aap.fordeling.arkiv.Fordeler.FordelingResultat
import no.nav.aap.fordeling.navorganisasjon.EnhetsKriteria.Status.AKTIV
import no.nav.aap.fordeling.navorganisasjon.NavEnhet
import no.nav.aap.util.Constants.AAP
import no.nav.aap.util.LoggerUtil
import no.nav.aap.util.LoggerUtil.getLogger
import org.springframework.stereotype.Component

@Component
class AAPFordeler(private val integrasjoner: Integrasjoner,private val manuell: AAPManuellFordeler) : Fordeler {

    private val log = getLogger(javaClass)
    override fun tema() = listOf(AAP)
    override fun fordel(jp: Journalpost): FordelingResultat {
        val navEnhet = navEnhet(jp).also { log.info("NAV-enhet er $it") }
        when (val brevkode = jp.dokumenter.first().brevkode) {
            STANDARD.kode -> fordelStandard(jp, navEnhet) // 2c
            STANDARD_ETTERSENDING.kode -> fordelEttersending(jp, navEnhet) // 2d
            else -> FordelingResultat("Ikke fordelt")
        }
    }

    private fun fordelEttersending(jp: Journalpost, navEnhet: NavEnhet) =
        integrasjoner.arena.hentNyesteAktiveSak().let {
            integrasjoner.arkiv.oppdaterOgFerdigstill(jp, it, navEnhet) // 3a/b
            FordelingResultat("Ettersending")
        }

    private fun fordelStandard(jp: Journalpost, navEnhet: NavEnhet) =
        if (!integrasjoner.arena.harAktivSak(jp)) { // 2c-1
            val sak = integrasjoner.arena.opprettStartVedtak()  // 2c-2
            integrasjoner.arkiv.oppdaterOgFerdigstill(jp, sak, navEnhet) // 3a/b
            FordelingResultat("OK")
        }
        else {
            manuell.fordel(jp)
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