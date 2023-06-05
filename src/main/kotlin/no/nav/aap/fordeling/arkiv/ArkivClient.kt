package no.nav.aap.fordeling.arkiv

import org.springframework.stereotype.Component
import no.nav.aap.fordeling.arkiv.dokarkiv.DokarkivWebClientAdapter
import no.nav.aap.fordeling.arkiv.journalpost.Journalpost
import no.nav.aap.fordeling.arkiv.saf.SAFGraphQLAdapter
import no.nav.aap.fordeling.arkiv.saf.SAFWebClientAdapter

@Component
class ArkivClient(private val dokarkiv : DokarkivWebClientAdapter, private val safdok : SAFWebClientAdapter, private val saf : SAFGraphQLAdapter) {

    fun hentSøknad(jp : Journalpost) = safdok.originalDokument(jp)

    fun hentJournalpost(jp : String) = saf.hentJournalpost(jp)

    fun oppdaterOgFerdigstillJournalpost(jp : Journalpost, sakNr : String) =
        dokarkiv.oppdaterOgFerdigstillJournalpost(jp, sakNr)

    override fun toString() = "ArkivClient(dokarkiv=$dokarkiv, saf=$saf)"
}