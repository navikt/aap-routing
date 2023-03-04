package no.nav.aap.fordeling.arkiv

import org.springframework.stereotype.Component

@Component
class ArkivClient(private val dokarkiv: ArkivWebClientAdapter, private val saf: ArkivGraphQLAdapter) {
    fun hentJournalpost(journalpostId: Long)  = saf.hentJournalpost(journalpostId)
    fun oppdaterOgFerdigstillJournalpost(journalpost: Journalpost, sakNr: String) =
        dokarkiv.oppdaterOgFerdigstillJournalpost(journalpost.journalpostId,journalpost.oppdateringsData(sakNr))
}