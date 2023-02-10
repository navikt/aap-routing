package no.nav.aap.fordeling.arkiv

import no.nav.aap.fordeling.navorganisasjon.NavEnhet
import org.springframework.stereotype.Component

@Component
class ArkivClient(private val a: ArkivWebClientAdapter) {
    fun journalpost(id: Long)  = a.journalpost(id)
    fun oppdaterJournalpost(jp: Journalpost, navEnhet: NavEnhet) = Unit // TODO
    fun ferdigstillJournalpost(jp: Journalpost)= Unit // TODO
}