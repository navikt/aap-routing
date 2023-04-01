package no.nav.aap.fordeling.navenhet

import org.springframework.stereotype.Component
import no.nav.aap.fordeling.arkiv.fordeling.Journalpost
import no.nav.aap.fordeling.person.PDLClient
import no.nav.aap.util.ExtensionUtils.whenNull
import no.nav.aap.util.LoggerUtil.getLogger

@Component
data class NavEnhetUtvelger(val pdl : PDLClient, val enhet : NavEnhetClient) {

    val log = getLogger(javaClass)

    fun navEnhet(jp : Journalpost) =
        jp.enhet?.let {
            if (enhet.erAktiv(it, enhet.aktiveEnheter())) {
                log.info("$enhet ER aktiv")
                it
            }
            else {
                log.info("$enhet er IKKE aktiv")
                enhetFor(jp)
            }
        } ?: enhetFor(jp)?.let {
            log.info("Journalførende enhet var ikke satt på journalpost ${jp.journalpostId}, fra GT oppslag er den ${it.enhetNr}")
            it
        }

    private fun enhetFor(jp : Journalpost) =
        enhet.navEnhet(pdl.geoTilknytning(jp.fnr), jp.egenAnsatt, jp.diskresjonskode, jp.tema).whenNull {
            log.warn("Ingen enhet for journalpost ${jp.journalpostId}")
        }
}