package no.nav.aap.fordeling.arena

import org.springframework.stereotype.Component
import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.fordeling.arkiv.fordeling.Journalpost
import no.nav.aap.fordeling.navenhet.EnhetsKriteria.NavOrg.NAVEnhet

@Component
class ArenaClient(private val adapter : ArenaWebClientAdapter) {

    fun harAktivSak(fnr : Fødselsnummer) = nyesteAktiveSak(fnr) != null

    fun opprettOppgave(jp : Journalpost, enhet : NAVEnhet) = adapter.opprettArenaOppgave(jp.opprettArenaOppgaveData(enhet), jp.journalpostId)

    fun nyesteAktiveSak(fnr : Fødselsnummer) = adapter.nyesteArenaSak(fnr)

    override fun toString() = "ArenaClient(adapter=$adapter)"
}