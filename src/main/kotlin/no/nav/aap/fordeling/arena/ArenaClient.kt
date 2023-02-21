package no.nav.aap.fordeling.arena

import no.nav.aap.fordeling.arkiv.Journalpost
import no.nav.aap.fordeling.arkiv.JournalpostDTO.OppdaterJournalpostForespørsel.Sak
import no.nav.aap.fordeling.navorganisasjon.NavEnhet
import org.springframework.stereotype.Component

@Component
class ArenaClient(private val a: ArenaWebClientAdapter) {
    fun harAktivSak(jp: Journalpost) = a.harAktivArenaSak(jp.fnr)
    fun opprettArenaOppgave(jp: Journalpost, enhet: NavEnhet) = a.opprettArenaOppgave(jp,enhet)
    fun nyesteAktiveSak(jp: Journalpost) = a.nyesteSak(jp.fnr)
}