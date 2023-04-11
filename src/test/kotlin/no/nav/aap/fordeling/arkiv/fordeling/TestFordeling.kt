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
import no.nav.aap.fordeling.arkiv.fordeling.Fordeler.Companion.FIKTIVTFNR
import no.nav.aap.fordeling.arkiv.fordeling.Fordeler.FordelingResultat.FordelingType.ALLEREDE_OPPGAVE
import no.nav.aap.fordeling.arkiv.fordeling.Fordeler.FordelingResultat.FordelingType.AUTOMATISK
import no.nav.aap.fordeling.arkiv.fordeling.Fordeler.FordelingResultat.FordelingType.MANUELL_FORDELING
import no.nav.aap.fordeling.arkiv.fordeling.Fordeler.FordelingResultat.FordelingType.MANUELL_JOURNALFØRING
import no.nav.aap.fordeling.arkiv.fordeling.TestData.ARENASAK
import no.nav.aap.fordeling.arkiv.fordeling.TestData.JP
import no.nav.aap.fordeling.arkiv.fordeling.TestData.JPES
import no.nav.aap.fordeling.arkiv.fordeling.TestData.OPPRETTET
import no.nav.aap.fordeling.arkiv.fordeling.TestData.UTLAND
import no.nav.aap.fordeling.navenhet.NAVEnhet.Companion.AUTOMATISK_JOURNALFØRING_ENHET
import no.nav.aap.fordeling.oppgave.OppgaveClient

@TestInstance(PER_CLASS)
class TestFordeling {

    val arena : ArenaClient = mock()
    val arkiv : ArkivClient = mock()
    val oppgave : OppgaveClient = mock()

    lateinit var fordeler : AAPFordeler

    @BeforeAll
    fun beforeAll() {
        fordeler = AAPFordeler(arena, arkiv, AAPManuellFordeler(oppgave))
    }

    @BeforeEach
    fun beforeEach() {
        reset(arena, arkiv, oppgave)
        whenever(arena.opprettOppgave(JP, AUTOMATISK_JOURNALFØRING_ENHET)).thenReturn(OPPRETTET)
    }

    @Test
    @DisplayName("Hovedsøknad uten aktiv arenasak fordeles automatisk")
    fun hovedsøknadUtenArenasak() {
        whenever(arena.harAktivSak(FIKTIVTFNR)).thenReturn(false)
        assertThat(fordeler.fordel(JP, AUTOMATISK_JOURNALFØRING_ENHET).fordelingstype).isEqualTo(AUTOMATISK)
        verify(arkiv).oppdaterOgFerdigstillJournalpost(JP, ARENASAK)
        verifyNoInteractions(oppgave)
        inOrder(arena, arkiv)
    }

    @Test
    @DisplayName("Hovedsøknad med eksisterende arenasak går til manuell journlføring")
    fun hovedSøknadMedArenasak() {
        whenever(oppgave.harOppgave(JP.id)).thenReturn(false)
        whenever(arena.harAktivSak(FIKTIVTFNR)).thenReturn(true)
        assertThat(fordeler.fordel(JP, AUTOMATISK_JOURNALFØRING_ENHET).fordelingstype).isEqualTo(MANUELL_JOURNALFØRING)
        verify(arena).harAktivSak(FIKTIVTFNR)
        verify(oppgave).harOppgave(JP.id)
        verify(oppgave).opprettJournalføringOppgave(JP, AUTOMATISK_JOURNALFØRING_ENHET)
        verifyNoInteractions(arkiv)
        verifyNoMoreInteractions(oppgave)
        verifyNoMoreInteractions(arena)
        inOrder(arena, oppgave)
    }

    @Test
    @DisplayName("Hovedsøknad arkivering feiler, oppreter journalføringsoppgave")
    fun automatiskFeiler() {
        whenever(oppgave.harOppgave(JP.id)).thenReturn(false)
        whenever(arena.harAktivSak(FIKTIVTFNR)).thenReturn(false)
        whenever(arkiv.oppdaterOgFerdigstillJournalpost(JP, ARENASAK)).thenThrow(IrrecoverableIntegrationException::class.java)
        assertThat(fordeler.fordel(JP, AUTOMATISK_JOURNALFØRING_ENHET).fordelingstype).isEqualTo(MANUELL_JOURNALFØRING)
        verify(arena).harAktivSak(FIKTIVTFNR)
        verify(arena).opprettOppgave(JP, AUTOMATISK_JOURNALFØRING_ENHET)
        verifyNoMoreInteractions(arena)
        verify(arkiv).oppdaterOgFerdigstillJournalpost(JP, ARENASAK)
        verifyNoMoreInteractions(arkiv)
        verify(oppgave).opprettJournalføringOppgave(JP, AUTOMATISK_JOURNALFØRING_ENHET)
        verify(oppgave).harOppgave(JP.id)
        verifyNoMoreInteractions(oppgave)
        inOrder(arena, arkiv, oppgave)
    }
    
    @Test
    @DisplayName("Manuell fordeling oppretter IKKE journalføringsoppgave når det allerede finnes en")
    fun journalføringsoppgaveFinnes() {
        whenever(arena.harAktivSak(FIKTIVTFNR)).thenReturn(true)
        whenever(oppgave.harOppgave(JP.id)).thenReturn(true)
        assertThat(fordeler.fordel(JP, AUTOMATISK_JOURNALFØRING_ENHET).fordelingstype).isEqualTo(ALLEREDE_OPPGAVE)
        verify(oppgave).harOppgave(JP.id)
        verifyNoMoreInteractions(oppgave)
    }

    @Test
    @DisplayName("Manuell fordeling oppretter fordelingsoppgave når enhet ikke er kjent")
    fun fordelingsOppgave() {
        assertThat(fordeler.fordel(JP, null).fordelingstype).isEqualTo(MANUELL_FORDELING)
        verify(oppgave).opprettFordelingOppgave(JP)
        verifyNoMoreInteractions(oppgave)
    }

    @Test
    @DisplayName("Manuell fordeling oppretter fordelingsoppgave når opprettelse av journalføringsoppgave feilet")
    fun fordelingsOppgaveVedException() {
        whenever(arena.harAktivSak(FIKTIVTFNR)).thenReturn(true)
        whenever(oppgave.opprettJournalføringOppgave(JP, AUTOMATISK_JOURNALFØRING_ENHET)).thenThrow(IrrecoverableIntegrationException::class.java)
        assertThat(fordeler.fordel(JP, AUTOMATISK_JOURNALFØRING_ENHET).fordelingstype).isEqualTo(MANUELL_FORDELING)
        verify(oppgave).harOppgave(JP.id)
        verify(oppgave).opprettFordelingOppgave(JP)
        verify(oppgave).opprettJournalføringOppgave(JP, AUTOMATISK_JOURNALFØRING_ENHET)
        verifyNoMoreInteractions(oppgave)
    }

    @Test
    @DisplayName("Ettersending som HAR Arena sak skal ferdigstille JP")
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

    @Test
    @DisplayName("Ikke-håndterte brevkoder fordeles manuelt")
    fun fordelIkkeHåndtertBrevkodeManuelt() {
        assertThat(fordeler.fordel(UTLAND, AUTOMATISK_JOURNALFØRING_ENHET).fordelingstype).isEqualTo(MANUELL_JOURNALFØRING)
        verify(oppgave).opprettJournalføringOppgave(UTLAND, AUTOMATISK_JOURNALFØRING_ENHET)
    }
}