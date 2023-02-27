package no.nav.aap.fordeling.arkiv

import no.nav.aap.fordeling.person.PDLWebClientAdapter
import org.springframework.stereotype.Component

@Component
class ArkivClient(private val a: ArkivWebClientAdapter) {
    fun hentJournalpost(journalpostId: Long)  =
        a.hentJournalpost(journalpostId)
    fun oppdaterOgFerdigstillJournalpost(journalpost: Journalpost, sakNr: String) =
        a.oppdaterOgFerdigstillJournalpost(journalpost.journalpostId,journalpost.oppdateringsData(sakNr))
}