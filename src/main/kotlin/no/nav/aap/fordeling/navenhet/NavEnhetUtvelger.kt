package no.nav.aap.fordeling.navenhet

import no.nav.aap.fordeling.arkiv.fordeling.Journalpost
import no.nav.aap.fordeling.navenhet.EnhetsKriteria.NavOrg.NAVEnhet
import no.nav.aap.fordeling.person.PDLClient
import no.nav.aap.util.ExtensionUtils.whenNull
import no.nav.aap.util.LoggerUtil.getLogger
import org.springframework.stereotype.Component

@Component
data class NavEnhetUtvelger(val pdl: PDLClient, val enhet: NavEnhetClient) {
    val log = getLogger(javaClass)

    fun navEnhet(jp: Journalpost) =
        jp.journalførendeEnhet?.let { e ->
            if (enhet.erAktiv(e,enhet.aktiveEnheter())) {
                NAVEnhet(e).also { log.info("Journalførende enhet ${it.enhetNr} satt på journalposten er aktiv")
                it}
            }
            else {
                enhetFor(jp)?.let { log.info("Journalførende enhet ${it.enhetNr} satt på journalposten er IKKE aktiv")
                it}
            }
        } ?: enhetFor(jp)?.let { log.info("Journalførende enhet ikke satt på journalposten, fra GT oppslag er den ${it.enhetNr}")
        it}

    private fun enhetFor(jp: Journalpost) =
        enhet.navEnhet(pdl.geoTilknytning(jp.fnr), jp.egenAnsatt, jp.diskresjonskode, jp.tema).whenNull { log.warn("Ingen enhet for journalpost ${jp.journalpostId
        }") }
}