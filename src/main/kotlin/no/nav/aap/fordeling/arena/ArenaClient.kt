package no.nav.aap.fordeling.arena

import no.nav.aap.fordeling.arkiv.Journalpost
import no.nav.aap.fordeling.navorganisasjon.NavEnhet
import org.springframework.stereotype.Component

@Component
class ArenaClient(private val a: ArenaWebClientAdapter) {
    fun harAktivSak(jp: Journalpost) = a.harAktivSak(jp.fnr)
    fun opprettStartVedtak(jp: Journalpost,enhet: NavEnhet) = a.opprettArenaSak(jp,enhet)
    fun hentNyesteAktiveSak(): Nothing  =  TODO()
}