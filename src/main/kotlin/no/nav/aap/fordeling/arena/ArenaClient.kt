package no.nav.aap.fordeling.arena

import no.nav.aap.fordeling.arkiv.Journalpost
import no.nav.aap.fordeling.navorganisasjon.NavEnhet
import org.springframework.stereotype.Component

@Component
class ArenaClient(private val adapter: ArenaWebClientAdapter) {
    fun harAktivSak(journalpost: Journalpost) = adapter.harAktivSak(journalpost.fnr)
    fun opprettStartVedtak() = Unit // TODO
    fun hentNyesteAktiveSak()  = Unit // TODO
}