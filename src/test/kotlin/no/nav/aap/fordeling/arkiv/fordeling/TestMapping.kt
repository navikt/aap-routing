package no.nav.aap.fordeling.arkiv.fordeling

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import org.mockito.Mockito.*
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import no.nav.aap.fordeling.arkiv.fordeling.JournalpostMapper.Companion.FIKTIVTFNR
import no.nav.aap.fordeling.arkiv.fordeling.TestData.AKTØR
import no.nav.aap.fordeling.arkiv.fordeling.TestData.DTO
import no.nav.aap.fordeling.egenansatt.EgenAnsattClient
import no.nav.aap.fordeling.person.Diskresjonskode.ANY
import no.nav.aap.fordeling.person.PDLClient

@TestInstance(PER_CLASS)
class TestMapping {

    val pdl : PDLClient = mock()
    val egen : EgenAnsattClient = mock()

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
        assertEquals(jp.fnr.fnr, DTO.avsenderMottaker?.id)
    }
}