package no.nav.aap.fordeling.arena

import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.fordeling.arkiv.fordeling.Journalpost
import no.nav.aap.fordeling.navenhet.EnhetsKriteria.NavOrg.NAVEnhet
import org.springframework.stereotype.Component

@Component
class ArenaClient(private val a: ArenaWebClientAdapter) {
    fun harAktivSak(fnr: Fødselsnummer) = nyesteAktiveSak(fnr) != null
    fun opprettOppgave(journalpost: Journalpost, enhet: NAVEnhet) =
        a.opprettArenaOppgave(journalpost.opprettArenaOppgaveData(enhet))

    fun nyesteAktiveSak(fnr: Fødselsnummer) = a.nyesteArenaSak(fnr)
    override fun toString(): String {
        return "ArenaClient(a=$a)"
    }
}