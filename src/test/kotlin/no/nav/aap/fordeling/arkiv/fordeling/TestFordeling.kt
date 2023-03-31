package no.nav.aap.fordeling.arkiv.fordeling

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import org.mockito.Mockito.*
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import no.nav.aap.api.felles.error.IrrecoverableIntegrationException
import no.nav.aap.fordeling.arena.ArenaClient
import no.nav.aap.fordeling.arkiv.ArkivClient
import no.nav.aap.fordeling.arkiv.fordeling.Fordeler.FordelerConfig.Companion.LOCAL
import no.nav.aap.fordeling.arkiv.fordeling.Fordeler.FordelerConfig.Companion.PROD_AAP
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.FordelingResultat.FordelingType.AUTOMATISK
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.FordelingResultat.FordelingType.INGEN
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.FordelingResultat.FordelingType.MANUELL_FORDELING
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.FordelingResultat.FordelingType.MANUELL_JOURNALFØRING
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.JournalpostDTO.JournalStatus.MOTTATT
import no.nav.aap.fordeling.arkiv.fordeling.JournalpostMapper.Companion.FIKTIVTFNR
import no.nav.aap.fordeling.arkiv.fordeling.TestData.ARENASAK
import no.nav.aap.fordeling.arkiv.fordeling.TestData.JP
import no.nav.aap.fordeling.arkiv.fordeling.TestData.JPES
import no.nav.aap.fordeling.arkiv.fordeling.TestData.OPPRETTET
import no.nav.aap.fordeling.navenhet.NAVEnhet.Companion.AUTOMATISK_JOURNALFØRING_ENHET
import no.nav.aap.fordeling.oppgave.OppgaveClient
import no.nav.aap.util.Constants.AAP

@TestInstance(PER_CLASS)
class TestFordeling {

    val arena : ArenaClient = mock()
    val arkiv : ArkivClient = mock()
    val oppgave : OppgaveClient = mock()
    val prodFordeler : AAPFordelerProd = mock()

    lateinit var fordeler : AAPFordeler
    lateinit var manuellFordeler : AAPManuellFordeler

    @BeforeAll
    fun beforeAll() {
        manuellFordeler = AAPManuellFordeler(oppgave, LOCAL)
        fordeler = AAPFordeler(arena, arkiv, ManuellFordelingFactory(listOf(manuellFordeler, AAPManuellFordelerProd(oppgave))), LOCAL)
    }

    @BeforeEach
    fun beforeEach() {
        reset(arena, arkiv, oppgave, prodFordeler)
        whenever(prodFordeler.cfg).thenReturn(PROD_AAP)
        whenever(arena.opprettOppgave(JP, AUTOMATISK_JOURNALFØRING_ENHET)).thenReturn(OPPRETTET)
    }

    @Test
    @DisplayName("Factory for hovedsøknad skal bruke riktig fordeler")
    fun factoryFordelerAutomatisk() {
        whenever(arena.harAktivSak(FIKTIVTFNR)).thenReturn(false)
        with(FordelingFactory(listOf(fordeler, prodFordeler))) {
            assertThat(kanFordele(AAP, MOTTATT.name))
            assertThat(fordel(JP, AUTOMATISK_JOURNALFØRING_ENHET).fordelingstype).isEqualTo(AUTOMATISK)
        }
        verify(arkiv).oppdaterOgFerdigstillJournalpost(JP, ARENASAK)
        verify(prodFordeler, times(3)).cfg
        verifyNoMoreInteractions(prodFordeler)
    }

    @Test
    @DisplayName("Factory for manuell fordeling av hovedsøknad oppretter fordelingsoppgave når opprettelse av journalføringsoppgave feilet")
    fun manuellFactoryFordelerManuelt() {
        whenever(oppgave.opprettJournalføringOppgave(JP, AUTOMATISK_JOURNALFØRING_ENHET)).thenThrow(IrrecoverableIntegrationException::class.java)
        assertThat(ManuellFordelingFactory(listOf(manuellFordeler, AAPManuellFordelerProd(oppgave))).fordel(JP,
            AUTOMATISK_JOURNALFØRING_ENHET).fordelingstype).isEqualTo(MANUELL_FORDELING)
        verify(oppgave).harOppgave(JP.journalpostId)
        verify(oppgave).opprettFordelingOppgave(JP)
        verify(oppgave).opprettJournalføringOppgave(JP, AUTOMATISK_JOURNALFØRING_ENHET)
        verifyNoMoreInteractions(oppgave)
    }

    @Test
    @DisplayName("Hhovedsøknad uten arenasak oppretter oppgave og journalfører automatisk")
    fun hovedSøknadUtenArenasak() {
        whenever(arena.harAktivSak(FIKTIVTFNR)).thenReturn(false)
        assertThat(fordeler.fordel(JP, AUTOMATISK_JOURNALFØRING_ENHET).fordelingstype).isEqualTo(AUTOMATISK)
        verify(arena).harAktivSak(FIKTIVTFNR)
        verify(arena).opprettOppgave(JP, AUTOMATISK_JOURNALFØRING_ENHET)
        verifyNoMoreInteractions(arena)
        verify(arkiv).oppdaterOgFerdigstillJournalpost(JP, ARENASAK)
    }

    @Test
    @DisplayName("Hovedsøknad arkivering feiler, går til manuell")
    fun automatiskFeiler() {
        whenever(oppgave.harOppgave(JP.journalpostId)).thenReturn(false)
        whenever(arena.harAktivSak(FIKTIVTFNR)).thenReturn(false)
        whenever(arkiv.oppdaterOgFerdigstillJournalpost(JP, ARENASAK)).thenThrow(IrrecoverableIntegrationException::class.java)
        assertThat(fordeler.fordel(JP, AUTOMATISK_JOURNALFØRING_ENHET).fordelingstype).isEqualTo(MANUELL_JOURNALFØRING)
        verify(arena).harAktivSak(FIKTIVTFNR)
        verify(arena).opprettOppgave(JP, AUTOMATISK_JOURNALFØRING_ENHET)
        verifyNoMoreInteractions(arena)
        verify(arkiv).oppdaterOgFerdigstillJournalpost(JP, ARENASAK)
        verifyNoMoreInteractions(arkiv)
        verify(oppgave).opprettJournalføringOppgave(JP, AUTOMATISK_JOURNALFØRING_ENHET)
        verify(oppgave).harOppgave(JP.journalpostId)
        verifyNoMoreInteractions(oppgave)
    }

    @Test
    @DisplayName("Hovedsøknad med eksisterende arenasak går til manuell fordeling")
    fun hovedSøknadMedArenasak() {
        whenever(arena.harAktivSak(FIKTIVTFNR)).thenReturn(true)
        assertThat(fordeler.fordel(JP, AUTOMATISK_JOURNALFØRING_ENHET).fordelingstype).isEqualTo(MANUELL_JOURNALFØRING)
        verify(arena).harAktivSak(FIKTIVTFNR)
        verify(oppgave).opprettJournalføringOppgave(JP, AUTOMATISK_JOURNALFØRING_ENHET)
        verifyNoInteractions(arkiv)
        verifyNoMoreInteractions(arena)
    }

    @Test
    @DisplayName("Manuell fordeling oppretter journalføringsoppgave når det ikke allerede finnes en")
    fun journalføringsOppgave() {
        whenever(oppgave.harOppgave(JP.journalpostId)).thenReturn(false)
        assertThat(manuellFordeler.fordel(JP, AUTOMATISK_JOURNALFØRING_ENHET).fordelingstype).isEqualTo(MANUELL_JOURNALFØRING)
        verify(oppgave).opprettJournalføringOppgave(JP, AUTOMATISK_JOURNALFØRING_ENHET)
        verify(oppgave).harOppgave(JP.journalpostId)
        verifyNoMoreInteractions(oppgave)
    }

    @Test
    @DisplayName("Manuell fordeling oppretter IKKE journalføringsoppgave når det allerede finnes en")
    fun journalføringsoppgaveFinnes() {
        whenever(oppgave.harOppgave(JP.journalpostId)).thenReturn(true)
        assertThat(manuellFordeler.fordel(JP, AUTOMATISK_JOURNALFØRING_ENHET).fordelingstype).isEqualTo(INGEN)
        verify(oppgave).harOppgave(JP.journalpostId)
        verifyNoMoreInteractions(oppgave)
    }

    @Test
    @DisplayName("Manuell fordeling oppretter fordelingsoppgave når enhet ikke er kjent")
    fun fordelingsOppgave() {
        assertThat(manuellFordeler.fordel(JP, null).fordelingstype).isEqualTo(MANUELL_FORDELING)
        verify(oppgave).opprettFordelingOppgave(JP)
        verifyNoMoreInteractions(oppgave)
    }

    @Test
    @DisplayName("Manuell fordeling oppretter fordelingsoppgave når opprettelse av journalføringsoppgave feilet")
    fun fordelingsOppgaveVedException() {
        whenever(oppgave.opprettJournalføringOppgave(JP, AUTOMATISK_JOURNALFØRING_ENHET)).thenThrow(IrrecoverableIntegrationException::class.java)
        assertThat(manuellFordeler.fordel(JP, AUTOMATISK_JOURNALFØRING_ENHET).fordelingstype).isEqualTo(MANUELL_FORDELING)
        verify(oppgave).harOppgave(JP.journalpostId)
        verify(oppgave).opprettFordelingOppgave(JP)
        verify(oppgave).opprettJournalføringOppgave(JP, AUTOMATISK_JOURNALFØRING_ENHET)
        verifyNoMoreInteractions(oppgave)
    }

    @Test
    @DisplayName("Ettersending som har Arena sak skal ferdigstille JP")
    fun fordelEttersendingAutomatisk() {
        whenever(arena.nyesteAktiveSak(FIKTIVTFNR)).thenReturn(ARENASAK)
        assertThat(fordeler.fordel(JPES, AUTOMATISK_JOURNALFØRING_ENHET).fordelingstype).isEqualTo(AUTOMATISK)
        verify(arkiv).oppdaterOgFerdigstillJournalpost(JPES, ARENASAK)
    }

    @Test
    @DisplayName("Ettersending som IKKE har Arena sak skal opprette journalføringsoppgave")
    fun fordelEttersendingManuellJournalføring() {
        whenever(arena.nyesteAktiveSak(FIKTIVTFNR)).thenReturn(null)
        assertThat(fordeler.fordel(JPES, AUTOMATISK_JOURNALFØRING_ENHET).fordelingstype).isEqualTo(MANUELL_JOURNALFØRING)
        verify(oppgave).opprettJournalføringOppgave(JPES, AUTOMATISK_JOURNALFØRING_ENHET)
    }

    @Test
    @DisplayName("Ettersending som IKKE har Arena sak og som ikke har enhet skal opprette fordelingsoppgave")
    fun fordelEttersendingManuellFordeling() {
        whenever(arena.nyesteAktiveSak(FIKTIVTFNR)).thenReturn(null)
        assertThat(fordeler.fordel(JPES, null).fordelingstype).isEqualTo(MANUELL_FORDELING)
        verify(oppgave).opprettFordelingOppgave(JPES)
    }
}