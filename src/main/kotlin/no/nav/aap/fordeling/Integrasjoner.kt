package no.nav.aap.fordeling

import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.fordeling.arena.ArenaClient
import no.nav.aap.fordeling.arkiv.ArkivClient
import no.nav.aap.fordeling.arkiv.fordeling.Journalpost
import no.nav.aap.fordeling.egenansatt.EgenAnsattClient
import no.nav.aap.fordeling.navorganisasjon.EnhetsKriteria.NAVEnhet
import no.nav.aap.fordeling.navorganisasjon.EnhetsKriteria.Status.AKTIV
import no.nav.aap.fordeling.navorganisasjon.NavOrgClient
import no.nav.aap.fordeling.oppgave.OppgaveClient
import no.nav.aap.fordeling.person.PDLClient
import no.nav.aap.util.LoggerUtil.getLogger
import org.springframework.stereotype.Component

@Component
data class Integrasjoner(val oppgave: OppgaveClient, val pdl: PDLClient, val org: NavOrgClient, val egen: EgenAnsattClient, val arena: ArenaClient, val arkiv: ArkivClient) {
    val log = getLogger(javaClass)

    fun navEnhet(journalpost: Journalpost) =
        with(journalpost) {
            journalførendeEnhet?.let { enhet ->
                if (org.erAktiv(enhet))
                    NAVEnhet(enhet, AKTIV).also { log.info("Journalførende enhet  $it satt på journalposten er aktiv") }
                else {
                    enhetFor(fnr).also { log.info("Enhet $it satt på journalposten er IKKE aktiv") }
                }
            }?: enhetFor(fnr).also { log.info("Enhet ikke satt på journalposten, fra GT er den $it") }
        }
    private fun enhetFor(fnr: Fødselsnummer) = org.navEnhet(pdl.geoTilknytning(fnr), egen.erSkjermet(fnr), pdl.diskresjonskode(fnr))
}