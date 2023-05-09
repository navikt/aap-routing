package no.nav.aap.fordeling.arkiv.fordeling

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import no.nav.aap.fordeling.arkiv.journalpost.Journalpost.JournalpostStatus
import no.nav.aap.fordeling.arkiv.journalpost.Journalpost.JournalpostStatus.JOURNALFØRT
import no.nav.aap.fordeling.arkiv.journalpost.Journalpost.JournalpostStatus.MOTTATT
import no.nav.aap.fordeling.fordeling.FordelingAvOppgaveUtvelger.FordelingsBeslutning
import no.nav.aap.fordeling.fordeling.FordelingAvOppgaveUtvelger.FordelingsBeslutning.ARENA
import no.nav.aap.fordeling.fordeling.FordelingAvOppgaveUtvelger.FordelingsBeslutning.GOSYS
import no.nav.aap.fordeling.fordeling.FordelingAvOppgaveUtvelger.FordelingsBeslutning.INGEN_DESTINASJON
import no.nav.aap.fordeling.arkiv.fordeling.TestData.JP
import no.nav.aap.fordeling.arkiv.fordeling.TestData.medStatus
import no.nav.aap.fordeling.arkiv.fordeling.TestData.somMeldekort
import no.nav.aap.fordeling.arkiv.fordeling.TestData.utenBruker
import no.nav.aap.fordeling.fordeling.ArenaBeslutter
import no.nav.aap.fordeling.arkiv.journalpost.Journalpost
import no.nav.aap.fordeling.fordeling.FordelingAvOppgaveUtvelger

class TestFordelingBeslutter {

    private val beslutter = FordelingAvOppgaveUtvelger(ArenaBeslutter())

    @Test
    fun testBeslutter() {
        expect(JP, ARENA)
        expect(JP.medStatus(JOURNALFØRT), INGEN_DESTINASJON)
        expect(JP.somMeldekort(), INGEN_DESTINASJON)
        expect(JP.utenBruker(), GOSYS)
        expect(JP, INGEN_DESTINASJON, JOURNALFØRT)
    }

    private fun expect(jp : Journalpost, status : FordelingsBeslutning, hendelsesStatus : JournalpostStatus = MOTTATT) =
        assertThat(beslutter.destinasjon(jp, hendelsesStatus, "topic")).isEqualTo(status)
}