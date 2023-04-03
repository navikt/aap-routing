package no.nav.aap.fordeling.arkiv

import org.springframework.stereotype.Component
import no.nav.aap.fordeling.arkiv.dokarkiv.DokarkivWebClientAdapter
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.JournalpostDTO.OppdateringDataDTO
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.JournalpostDTO.OppdateringDataDTO.SakDTO
import no.nav.aap.fordeling.arkiv.fordeling.Journalpost
import no.nav.aap.fordeling.arkiv.fordeling.JournalpostMapper.Companion.toDTO
import no.nav.aap.fordeling.arkiv.saf.SAFGraphQLAdapter
import no.nav.aap.util.LoggerUtil

@Component
class ArkivClient(private val dokarkiv : DokarkivWebClientAdapter, private val saf : SAFGraphQLAdapter) {

    val log = LoggerUtil.getLogger(ArkivClient::class.java)

    fun hentSøknad(jp : Journalpost) = saf.søknad(jp)

    fun hentJournalpost(jp : String) = saf.hentJournalpost(jp)
    fun oppdaterOgFerdigstillJournalpost(jp : Journalpost, sakNr : String) =
        dokarkiv.oppdaterOgFerdigstillJournalpost(jp.id, jp.oppdateringsData(sakNr))

    private fun Journalpost.oppdateringsData(saksNr : String) =
        OppdateringDataDTO(tittel, avsenderMottager?.toDTO() ?: bruker?.toDTO(), bruker?.toDTO(), SakDTO(saksNr), tema.uppercase())

    override fun toString() = "ArkivClient(dokarkiv=$dokarkiv, saf=$saf)"
}