package no.nav.aap.fordeling.arkiv.fordeling

import no.nav.aap.api.felles.AktørId
import no.nav.aap.api.felles.SkjemaType.STANDARD
import no.nav.aap.fordeling.arena.ArenaDTOs.ArenaOpprettetOppgave
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.JournalpostDTO
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.JournalpostDTO.Bruker
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.JournalpostDTO.BrukerDTO
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.JournalpostDTO.BrukerDTO.BrukerType.AKTOERID
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.JournalpostDTO.BrukerDTO.BrukerType.FNR
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.JournalpostDTO.DokumentInfo
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.JournalpostDTO.JournalStatus
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.JournalpostDTO.JournalStatus.MOTTATT
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.JournalpostDTO.JournalpostType.I
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.JournalpostDTO.Kanal
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.JournalpostDTO.Kanal.NAV_NO
import no.nav.aap.fordeling.arkiv.fordeling.JournalpostMapper.Companion.FIKTIVTFNR
import no.nav.aap.fordeling.navenhet.EnhetsKriteria.NavOrg.NAVEnhet.Companion.AUTO_ENHET
import no.nav.aap.util.Constants.AAP

object TestData {

    val AKTØR = AktørId("1111111111111")
    val ARENASAK = "456"
    val OPPRETTET = ArenaOpprettetOppgave("123", ARENASAK)
    val DOCS = setOf(DokumentInfo("123", STANDARD.tittel, STANDARD.kode))
    val JP = Journalpost(STANDARD.tittel, AUTO_ENHET, "42", MOTTATT, I, AAP,
        null, FIKTIVTFNR, Bruker(FIKTIVTFNR), AvsenderMottaker(FIKTIVTFNR), NAV_NO,
        emptySet(), DOCS)

    val DTO = JournalpostDTO(STANDARD.tittel, AUTO_ENHET, "42", MOTTATT, I, AAP,
        null, BrukerDTO(AKTØR.id, AKTOERID), AvsenderMottakerDTO(FIKTIVTFNR.fnr, FNR), NAV_NO, emptySet(), DOCS)

    fun Journalpost.withStatus(status : JournalStatus) = copy(status = status)
    fun Journalpost.withKanal(kanal : Kanal) = copy(kanal = kanal)
    fun Journalpost.meldekort() = copy(tittel = "Meldekort")

    fun JournalpostDTO.utenBruker() = copy(bruker = null)
}