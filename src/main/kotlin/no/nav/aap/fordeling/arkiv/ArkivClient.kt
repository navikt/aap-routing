package no.nav.aap.fordeling.arkiv

import no.nav.aap.fordeling.arena.ArenaDTOs.ArenaOpprettetOppgave
import no.nav.aap.fordeling.navorganisasjon.NavEnhet
import org.springframework.stereotype.Component

@Component
class ArkivClient(private val a: ArkivWebClientAdapter) {
    fun journalpost(id: Long)  = a.journalpost(id)
    fun oppdaterOgFerdigstill(jp: Journalpost, sak: ArenaOpprettetOppgave, enhet: NavEnhet) = a.oppdaterOgFerdigstill(jp, sak.arenaSakId,enhet.enhetNr)
}