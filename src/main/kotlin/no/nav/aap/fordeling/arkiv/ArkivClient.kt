package no.nav.aap.fordeling.arkiv

import no.nav.aap.fordeling.arkiv.dokarkiv.DokarkivWebClientAdapter
import no.nav.aap.fordeling.arkiv.fordeling.FordelingHendelseKonsument
import no.nav.aap.fordeling.arkiv.fordeling.Journalpost
import no.nav.aap.fordeling.arkiv.saf.SAFGraphQLAdapter
import no.nav.aap.fordeling.config.GlobalBeanConfig.Companion.whenNull
import no.nav.aap.util.LoggerUtil
import org.springframework.stereotype.Component

@Component
class ArkivClient(private val dokarkiv: DokarkivWebClientAdapter, private val saf: SAFGraphQLAdapter) {

    val log = LoggerUtil.getLogger(ArkivClient::class.java)

    fun hentJournalpost(journalpostId: String) = saf.hentJournalpost(journalpostId).whenNull {
        log.warn("Ingen journalpost kunne hentes for journalpost $journalpostId")
    }
    fun oppdaterOgFerdigstillJournalpost(journalpost: Journalpost, sakNr: String) =
        dokarkiv.oppdaterOgFerdigstillJournalpost(journalpost.journalpostId, journalpost.oppdateringsData(sakNr))
}