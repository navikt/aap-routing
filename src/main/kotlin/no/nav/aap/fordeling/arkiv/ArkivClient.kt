package no.nav.aap.fordeling.arkiv

import no.nav.aap.fordeling.arkiv.dokarkiv.DokarkivWebClientAdapter
import no.nav.aap.fordeling.arkiv.fordeling.Journalpost
import no.nav.aap.fordeling.arkiv.saf.SafGraphQLAdapter
import org.springframework.stereotype.Component

@Component
class ArkivClient(private val dokarkiv: DokarkivWebClientAdapter, private val saf: SafGraphQLAdapter) {
    fun hentJournalpost(journalpostId: Long)  = saf.hentJournalpost(journalpostId)
    fun oppdaterOgFerdigstillJournalpost(journalpost: Journalpost, sakNr: String) =
        dokarkiv.oppdaterOgFerdigstillJournalpost(journalpost.journalpostId,journalpost.oppdateringsData(sakNr))
}