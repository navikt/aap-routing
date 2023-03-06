package no.nav.aap.fordeling.arkiv

import no.nav.aap.fordeling.arkiv.dokarkiv.DokarkivWebClientAdapter
import no.nav.aap.fordeling.arkiv.fordeling.Journalpost
import no.nav.aap.fordeling.arkiv.saf.SAFGraphQLAdapter
import org.springframework.stereotype.Component

@Component
class ArkivClient(private val dokarkiv: DokarkivWebClientAdapter, private val saf: SAFGraphQLAdapter) {
    fun hentJournalpost(journalpostId: String) = saf.hentJournalpost(journalpostId)
    fun oppdaterOgFerdigstillJournalpost(journalpost: Journalpost, sakNr: String) =
        dokarkiv.oppdaterOgFerdigstillJournalpost(journalpost.journalpostId, journalpost.oppdateringsData(sakNr))
}