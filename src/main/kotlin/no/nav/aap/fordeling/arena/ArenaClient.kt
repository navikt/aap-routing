package no.nav.aap.fordeling.arena

import no.nav.aap.fordeling.arkiv.Journalpost
import no.nav.aap.fordeling.navorganisasjon.EnhetsKriteria.NavEnhet
import org.springframework.stereotype.Component

@Component
class ArenaClient(private val a: ArenaWebClientAdapter) {
    fun harAktivSak(journalpost: Journalpost) =
        nyesteAktiveSak(journalpost) != null
    fun opprettOppgave(journalpost: Journalpost, enhet: NavEnhet) =
        a.opprettArenaOppgave(journalpost.opprettArenaOppgaveData(enhet))
    fun nyesteAktiveSak(journalpost: Journalpost) =
        a.nyesteArenaSak(journalpost.fnr)
}