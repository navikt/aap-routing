package no.nav.aap.fordeling.arena

import io.micrometer.observation.annotation.Observed
import org.springframework.stereotype.Component
import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.fordeling.arkiv.fordeling.Journalpost
import no.nav.aap.fordeling.navenhet.NAVEnhet

@Component
@Observed
class ArenaClient(private val adapter : ArenaWebClientAdapter) {

    fun harAktivSak(fnr : Fødselsnummer) = nyesteAktiveSak(fnr) != null

    fun opprettOppgave(jp : Journalpost, enhet : NAVEnhet) = adapter.opprettArenaOppgave(jp, enhet.enhetNr)

    fun nyesteAktiveSak(fnr : Fødselsnummer) = adapter.nyesteArenaSak(fnr)

    override fun toString() = "ArenaClient(adapter=$adapter)"
}