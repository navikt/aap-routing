package no.nav.aap.fordeling.navenhet

import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.fordeling.arkiv.fordeling.Journalpost
import no.nav.aap.fordeling.egenansatt.EgenAnsattClient
import no.nav.aap.fordeling.navenhet.EnhetsKriteria.NavOrg.NAVEnhet
import no.nav.aap.fordeling.person.PDLClient
import no.nav.aap.util.LoggerUtil.getLogger
import org.springframework.stereotype.Component

@Component
data class NavEnhetUtvelger(val pdl: PDLClient, val enhet: NavEnhetClient, val egen: EgenAnsattClient) {
    val log = getLogger(javaClass)

    fun navEnhet(jp: Journalpost) =
        jp.journalførendeEnhet?.let { e ->
            if (enhet.erAktiv(e)) {
                NAVEnhet(e).also { log.info("Journalførende enhet ${it.enhetNr} satt på journalposten er aktiv") }
            }
            else {
                enhetFor(jp.fnr, jp.tema).also { log.info("Journalførende enhet ${it.enhetNr} satt på journalposten er IKKE aktiv") }
            }
        } ?: enhetFor(jp.fnr,jp.tema).also { log.info("Journalførende enhet ikke satt på journalposten, fra GT oppslag er den ${it.enhetNr}") }

    private fun enhetFor(fnr: Fødselsnummer, tema: String) =
        enhet.navEnhet(pdl.geoTilknytning(fnr), egen.erSkjermet(fnr), pdl.diskresjonskode(fnr), tema)
}