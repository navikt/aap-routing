package no.nav.aap.fordeling.arkiv.fordeling

import no.nav.aap.api.felles.AktørId
import no.nav.aap.api.felles.SkjemaType
import no.nav.aap.api.felles.SkjemaType.STANDARD
import no.nav.aap.api.felles.SkjemaType.STANDARD_ETTERSENDING
import no.nav.aap.api.felles.SkjemaType.UTLAND_SØKNAD
import no.nav.aap.fordeling.arena.ArenaDTOs.ArenaOpprettetOppgave
import no.nav.aap.fordeling.arkiv.fordeling.Fordeler.Companion.FIKTIVTFNR
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.DokumentVariant
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.DokumentVariant.VariantFormat
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.JournalpostDTO
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.JournalpostDTO.AvsenderMottakerDTO
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.JournalpostDTO.BrukerDTO
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.JournalpostDTO.DokumentInfoDTO
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.JournalpostDTO.IDTypeDTO.AKTOERID
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.JournalpostDTO.IDTypeDTO.FNR
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.JournalpostDTO.JournalStatusDTO
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.JournalpostDTO.Kanal
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.JournalpostDTO.Kanal.NAV_NO
import no.nav.aap.fordeling.arkiv.fordeling.Journalpost.Bruker
import no.nav.aap.fordeling.arkiv.fordeling.Journalpost.JournalpostStatus
import no.nav.aap.fordeling.arkiv.fordeling.Journalpost.JournalpostStatus.MOTTATT
import no.nav.aap.fordeling.arkiv.fordeling.JournalpostMapper.Companion.toDomain
import no.nav.aap.fordeling.navenhet.NAVEnhet.Companion.AUTOMATISK_JOURNALFØRING_ENHET
import no.nav.aap.fordeling.navenhet.NAVEnhet.Companion.AUTO_ENHET
import no.nav.aap.util.Constants.AAP

object TestData {

    val AKTØR = AktørId("1111111111111")
    val ARENASAK = "456"
    val OPPRETTET = ArenaOpprettetOppgave("123", ARENASAK)
    val DOCDTO = DokumentInfoDTO("123", STANDARD.tittel, STANDARD.kode, listOf(DokumentVariant(VariantFormat.ORIGINAL)))
    val DOCDTOS = setOf(DOCDTO)
    val JP = Journalpost("42", MOTTATT, AUTOMATISK_JOURNALFØRING_ENHET, STANDARD.tittel, AAP,
        null, FIKTIVTFNR, Bruker(FIKTIVTFNR), AvsenderMottaker(FIKTIVTFNR), NAV_NO, DOCDTOS.toDomain())
    val JPES = somSkjema(STANDARD_ETTERSENDING)

    val UTLAND = somSkjema(UTLAND_SØKNAD)

    val DTO = JournalpostDTO(STANDARD.tittel, AUTO_ENHET, "42", JournalStatusDTO.MOTTATT, AAP,
        null, BrukerDTO(AKTØR.id, AKTOERID), AvsenderMottakerDTO(FIKTIVTFNR.fnr, FNR), NAV_NO, DOCDTOS)

    private fun somSkjema(skjema : SkjemaType) = JP.copy(dokumenter = setOf(JP.hovedDokument.copy(tittel = skjema.tittel, brevkode = skjema.kode)))

    fun Journalpost.medStatus(status : JournalpostStatus) = copy(status = status)

    fun Journalpost.medKanal(kanal : Kanal) = copy(kanal = kanal)

    fun Journalpost.utenEnhet() = copy(enhet = null)

    fun Journalpost.utenBruker() = copy(bruker = null)

    fun Journalpost.somMeldekort() = copy(tittel = "Meldekort")
}