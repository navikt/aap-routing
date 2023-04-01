package no.nav.aap.fordeling.arkiv.fordeling

import no.nav.aap.api.felles.AktørId
import no.nav.aap.api.felles.SkjemaType
import no.nav.aap.api.felles.SkjemaType.STANDARD
import no.nav.aap.api.felles.SkjemaType.STANDARD_ETTERSENDING
import no.nav.aap.api.felles.SkjemaType.UTLAND_SØKNAD
import no.nav.aap.fordeling.arena.ArenaDTOs.ArenaOpprettetOppgave
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.JournalpostDTO
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.JournalpostDTO.BrukerDTO
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.JournalpostDTO.BrukerDTO.BrukerTypeDTO.AKTOERID
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.JournalpostDTO.BrukerDTO.BrukerTypeDTO.FNR
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.JournalpostDTO.DokumentInfoDTO
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.JournalpostDTO.JournalStatusDTO
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.JournalpostDTO.JournalpostTypeDTO.I
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.JournalpostDTO.Kanal
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.JournalpostDTO.Kanal.NAV_NO
import no.nav.aap.fordeling.arkiv.fordeling.Journalpost.Bruker
import no.nav.aap.fordeling.arkiv.fordeling.Journalpost.JournalpostStatus
import no.nav.aap.fordeling.arkiv.fordeling.Journalpost.JournalpostStatus.MOTTATT
import no.nav.aap.fordeling.arkiv.fordeling.JournalpostMapper.Companion.FIKTIVTFNR
import no.nav.aap.fordeling.navenhet.NAVEnhet.Companion.AUTOMATISK_JOURNALFØRING_ENHET
import no.nav.aap.fordeling.navenhet.NAVEnhet.Companion.AUTO_ENHET
import no.nav.aap.util.Constants.AAP

object TestData {

    val AKTØR = AktørId("1111111111111")
    val ARENASAK = "456"
    val OPPRETTET = ArenaOpprettetOppgave("123", ARENASAK)
    val DOC = DokumentInfoDTO("123", STANDARD.tittel, STANDARD.kode)
    val DOCS = setOf(DOC)
    val JP = Journalpost(STANDARD.tittel, AUTOMATISK_JOURNALFØRING_ENHET, "42", MOTTATT, AAP,
        null, FIKTIVTFNR, Bruker(FIKTIVTFNR), AvsenderMottaker(FIKTIVTFNR), NAV_NO, DOCS)
    val JPES = somSkjema(STANDARD_ETTERSENDING)

    val UTLAND = somSkjema(UTLAND_SØKNAD)

    val DTO = JournalpostDTO(STANDARD.tittel, AUTO_ENHET, "42", JournalStatusDTO.MOTTATT, I, AAP,
        null, BrukerDTO(AKTØR.id, AKTOERID), AvsenderMottakerDTO(FIKTIVTFNR.fnr, FNR), NAV_NO, emptySet(), DOCS)

    private fun somSkjema(skjema : SkjemaType) = JP.copy(dokumenter = setOf(DOC.copy(tittel = skjema.tittel, brevkode = skjema.kode)))

    fun Journalpost.medStatus(status : JournalpostStatus) = copy(status = status)

    fun Journalpost.medKanal(kanal : Kanal) = copy(kanal = kanal)

    fun Journalpost.utenEnhet() = copy(enhet = null)

    fun Journalpost.somMeldekort() = copy(tittel = "Meldekort")
}