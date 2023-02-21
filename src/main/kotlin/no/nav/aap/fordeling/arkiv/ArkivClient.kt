package no.nav.aap.fordeling.arkiv

import org.springframework.stereotype.Component

@Component
class ArkivClient(private val a: ArkivWebClientAdapter) {
    fun hentJournalpost(id: Long)  = a.hentJournalpost(id)
    fun oppdaterOgFerdigstillJournalpost(jp: Journalpost, sakNr: String) = a.oppdaterOgFerdigstillJournalpost(jp.journalpostId,jp.oppdateringsData(sakNr))
}