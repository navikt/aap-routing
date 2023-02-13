package no.nav.aap.fordeling.arkiv

import no.nav.aap.api.felles.SkjemaType.*
import no.nav.aap.fordeling.arkiv.Fordeler.FordelingResultat
import no.nav.aap.fordeling.navorganisasjon.EnhetsKriteria.Status.AKTIV
import no.nav.aap.fordeling.navorganisasjon.NavEnhet
import no.nav.aap.util.Constants.AAP
import no.nav.aap.util.LoggerUtil
import org.springframework.stereotype.Component

@Component
class AAPFordeler(private val integrasjoner: Integrasjoner) : Fordeler {

    private val log = LoggerUtil.getLogger(javaClass)
    override fun tema() = listOf(AAP)
    override fun fordel(jp: Journalpost): FordelingResultat {
        log.info("Fordeler ${jp.tema}")
        val navEnhet = navEnhet(jp).also { log.info("NAV-enhet er $it") }
        var brevkode = jp.dokumenter.first().brevkode // alltid først
        with(integrasjoner) {
            when (brevkode)  {
                STANDARD.kode -> { // 2c
                    if (arena.harAktivSak(jp)) { // 2c-1
                        val sak = arena.opprettStartVedtak()  // 2c-2
                        arkiv.oppdaterOgFerdigstill(jp,sak) // 3a/b
                    }
                    else {
                        throw FordelingException("Har arenasak",jp)
                    }
                }
                STANDARD_ETTERSENDING.kode -> { // 2d
                     arena.hentNyesteAktiveSak()?.let {
                         arkiv.oppdaterOgFerdigstill(jp,it) // 3a/b
                     }
                }
                else -> throw FordelingException("Ukjent brekode $brevkode")
            }
            //throw FordelingException("TESTING 123",jp)
            return FordelingResultat("OK")
        }

    }

    private fun navEnhet(jp: Journalpost) =
        jp.journalførendeEnhet?.let {
            if (integrasjoner.org.erAktiv(it))
                NavEnhet(it, AKTIV)
            else {
                enhetFor(jp)
            }
        }?: enhetFor(jp)

    private fun enhetFor(jp: Journalpost) =
        with(integrasjoner)  {
            org.navEnhet(pdl.geoTilknytning(jp.fnr), egen.erSkjermet(jp.fnr), pdl.diskresjonskode(jp.fnr))
        }
}