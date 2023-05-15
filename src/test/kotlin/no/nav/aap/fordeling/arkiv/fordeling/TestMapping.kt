package no.nav.aap.fordeling.arkiv.fordeling

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import no.nav.aap.fordeling.arkiv.fordeling.TestData.AKTØR
import no.nav.aap.fordeling.arkiv.fordeling.TestData.DTO
import no.nav.aap.fordeling.arkiv.journalpost.JournalpostMapper
import no.nav.aap.fordeling.egenansatt.EgenAnsattClient
import no.nav.aap.fordeling.fordeling.Fordeler.Companion.FIKTIVTFNR
import no.nav.aap.fordeling.person.Diskresjonskode.ANY
import no.nav.aap.fordeling.person.PDLClient

@TestInstance(PER_CLASS)
@ExtendWith(MockitoExtension::class)
class TestMapping {

    @Mock
    lateinit var pdl : PDLClient

    @Mock
    lateinit var egen : EgenAnsattClient

    @Test
    @DisplayName("Mapper skal veksle inn aktørId, sette egen ansatt")
    fun mapOK() {
        whenever(pdl.fnr(AKTØR)).thenReturn(FIKTIVTFNR)
        whenever(pdl.diskresjonskode(FIKTIVTFNR)).thenReturn(ANY)
        whenever(egen.erEgenAnsatt(FIKTIVTFNR)).thenReturn(true)
        val jp = JournalpostMapper(pdl, egen).tilJournalpost(DTO)
        verify(egen).erEgenAnsatt(FIKTIVTFNR)
        verify(pdl).fnr(AKTØR)
        assertEquals(jp.egenAnsatt, true)
        assertEquals(jp.tilVikafossen, true)
        assertEquals(jp.fnr.fnr, DTO.avsenderMottaker?.id)
    }
}