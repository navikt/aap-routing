package no.nav.aap.fordeling.arena

import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.fordeling.arkiv.Journalpost
import no.nav.aap.fordeling.navorganisasjon.EnhetsKriteria.NavEnhet
import org.springframework.stereotype.Component

@Component
class ArenaClient(private val a: ArenaWebClientAdapter) {
    fun harAktivSak(fnr: Fødselsnummer) =
        nyesteAktiveSak(fnr) != null
    fun opprettOppgave(journalpost: Journalpost, enhet: NavEnhet) =
        a.opprettArenaOppgave(journalpost.opprettArenaOppgaveData(enhet))
    fun nyesteAktiveSak(fnr: Fødselsnummer) =
        a.nyesteArenaSak(fnr)
}