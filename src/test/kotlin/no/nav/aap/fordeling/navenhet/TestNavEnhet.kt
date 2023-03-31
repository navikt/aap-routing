package no.nav.aap.fordeling.navenhet

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import org.mockito.Mockito.*
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import no.nav.aap.fordeling.arkiv.fordeling.JournalpostMapper.Companion.FIKTIVTFNR
import no.nav.aap.fordeling.arkiv.fordeling.TestData.JP
import no.nav.aap.fordeling.arkiv.fordeling.TestData.utenEnhet
import no.nav.aap.fordeling.navenhet.NAVEnhet.Companion.AUTOMATISK_JOURNALFØRING_ENHET
import no.nav.aap.fordeling.person.PDLClient

@TestInstance(PER_CLASS)
class TestNavEnhet {

    val enhet : NavEnhetClient = mock()
    val pdl : PDLClient = mock()

    @BeforeEach
    fun resetMocks() {
        reset(enhet, pdl)
    }

    @Test
    @DisplayName("Skal ikke slå opp navenhet om den er satt på JP og aktiv")
    fun ikkeSlåOppOmAktiv() {
        whenever(enhet.erAktiv(AUTOMATISK_JOURNALFØRING_ENHET, listOf(AUTOMATISK_JOURNALFØRING_ENHET))).thenReturn(true)
        whenever(enhet.aktiveEnheter()).thenReturn(listOf(AUTOMATISK_JOURNALFØRING_ENHET))
        assertEquals(AUTOMATISK_JOURNALFØRING_ENHET, NavEnhetUtvelger(pdl, enhet).navEnhet(JP))
        verifyNoInteractions(pdl)
        verify(enhet).aktiveEnheter()
        verify(enhet).erAktiv(AUTOMATISK_JOURNALFØRING_ENHET, listOf(AUTOMATISK_JOURNALFØRING_ENHET))
        verifyNoMoreInteractions(enhet)
    }

    @Test
    @DisplayName("Skal slå opp navenhet om den er satt på JP men ikke aktiv")
    fun slåOppOmIKKEAktiv() {
        whenever(enhet.erAktiv(AUTOMATISK_JOURNALFØRING_ENHET, emptyList())).thenReturn(false)
        whenever(enhet.aktiveEnheter()).thenReturn(emptyList())
        whenever(enhet.navEnhet("gtKommune", JP.egenAnsatt, JP.diskresjonskode, JP.tema)).thenReturn(AUTOMATISK_JOURNALFØRING_ENHET)
        whenever(pdl.geoTilknytning(FIKTIVTFNR)).thenReturn("gtKommune")
        NavEnhetUtvelger(pdl, enhet).navEnhet(JP)
        verify(pdl).geoTilknytning(FIKTIVTFNR)
        verifyNoMoreInteractions(pdl)
        verify(enhet).aktiveEnheter()
        verify(enhet).erAktiv(AUTOMATISK_JOURNALFØRING_ENHET, emptyList())
        verify(enhet).navEnhet("gtKommune", JP.egenAnsatt, JP.diskresjonskode, JP.tema)
        verifyNoMoreInteractions(enhet)
    }

    @Test
    @DisplayName("Skal slå opp navenhet om den ikke er satt på JP")
    fun slåOppOmIKKESatt() {
        whenever(enhet.navEnhet("gtKommune", JP.egenAnsatt, JP.diskresjonskode, JP.tema)).thenReturn(AUTOMATISK_JOURNALFØRING_ENHET)
        whenever(pdl.geoTilknytning(FIKTIVTFNR)).thenReturn("gtKommune")
        NavEnhetUtvelger(pdl, enhet).navEnhet(JP.utenEnhet())
        verify(pdl).geoTilknytning(FIKTIVTFNR)
        verifyNoMoreInteractions(pdl)
        verify(enhet).navEnhet("gtKommune", JP.egenAnsatt, JP.diskresjonskode, JP.tema)
        verifyNoMoreInteractions(enhet)
    }
}