package no.nav.aap.fordeling.arkiv

import no.nav.aap.fordeling.arena.ArenaDTOs.ArenaOpprettetOppgave
import org.springframework.stereotype.Component

@Component
class ArkivClient(private val a: ArkivWebClientAdapter) {
    fun journalpost(id: Long)  = a.hentJournalpost(id)
    fun oppdaterOgFerdigstill(jp: Journalpost, sak: ArenaOpprettetOppgave) = a.oppdaterOgFerdigstill(jp.journalpostId,jp.oppdateringsData(sak.arenaSakId))
}