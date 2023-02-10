package no.nav.aap.fordeling.arkiv

import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.felles.SkjemaType
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
                STANDARD.kode -> {
                    if (arena.harArenaSak(jp,navEnhet)) {
                        arena.opprettStartVedtak()
                    }
                }
                STANDARD_ETTERSENDING.kode -> {
                    arena.hentNyesteAktiveSak()
                }
                else -> throw FordelingException("Ukjent brekode $brevkode")
            }
            arkiv.oppdaterJournalpost(jp,navEnhet)
            arkiv.ferdigstillJournalpost(jp)
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