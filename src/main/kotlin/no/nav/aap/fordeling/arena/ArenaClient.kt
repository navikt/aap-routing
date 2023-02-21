package no.nav.aap.fordeling.arena

import no.nav.aap.fordeling.arkiv.Journalpost
import no.nav.aap.fordeling.navorganisasjon.NavEnhet
import org.springframework.stereotype.Component

@Component
class ArenaClient(private val a: ArenaWebClientAdapter) {
    fun harAktivArenaSak(jp: Journalpost) = nyesteAktiveSak(jp) != null
    fun opprettArenaOppgave(jp: Journalpost, enhet: NavEnhet) = a.opprettArenaOppgave(jp,enhet)
    fun nyesteAktiveSak(jp: Journalpost) = a.nyesteArenaSak(jp.fnr)
}