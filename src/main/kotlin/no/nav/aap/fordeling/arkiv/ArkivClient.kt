package no.nav.aap.fordeling.arkiv

import io.micrometer.observation.annotation.Observed
import org.springframework.stereotype.Component
import no.nav.aap.fordeling.arkiv.dokarkiv.DokarkivWebClientAdapter
import no.nav.aap.fordeling.arkiv.fordeling.Journalpost
import no.nav.aap.fordeling.arkiv.saf.SAFGraphQLAdapter
import no.nav.aap.fordeling.arkiv.saf.SAFWebClientAdapter
import no.nav.aap.util.LoggerUtil

@Component
@Observed(contextualName = "Arkiv")
class ArkivClient(private val dokarkiv : DokarkivWebClientAdapter, private val safdok : SAFWebClientAdapter, private val saf : SAFGraphQLAdapter) {

    val log = LoggerUtil.getLogger(ArkivClient::class.java)

    fun hentSøknad(jp : Journalpost) = safdok.originalDokument(jp)

    fun hentJournalpost(jp : String) = runCatching {
        saf.hentJournalpost1(jp).also {
            log.info("spring graphql ok")
        }

    }.getOrElse {
        log.warn("spring graphql jp feil", it)
        saf.hentJournalpost(jp)
    }

    fun oppdaterOgFerdigstillJournalpost(jp : Journalpost, sakNr : String) =
        dokarkiv.oppdaterOgFerdigstillJournalpost(jp, sakNr)

    override fun toString() = "ArkivClient(dokarkiv=$dokarkiv, saf=$saf)"
}