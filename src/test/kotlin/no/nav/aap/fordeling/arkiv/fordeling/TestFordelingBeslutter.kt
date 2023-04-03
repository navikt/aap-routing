package no.nav.aap.fordeling.arkiv.fordeling

import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import no.nav.aap.fordeling.arkiv.fordeling.FordelingBeslutter.FordelingsBeslutning
import no.nav.aap.fordeling.arkiv.fordeling.FordelingBeslutter.FordelingsBeslutning.INGEN_FORDELING
import no.nav.aap.fordeling.arkiv.fordeling.FordelingBeslutter.FordelingsBeslutning.TIL_ARENA
import no.nav.aap.fordeling.arkiv.fordeling.FordelingBeslutter.FordelingsBeslutning.TIL_GOSYS
import no.nav.aap.fordeling.arkiv.fordeling.Journalpost.JournalpostStatus.JOURNALFØRT
import no.nav.aap.fordeling.arkiv.fordeling.TestData.JP
import no.nav.aap.fordeling.arkiv.fordeling.TestData.medStatus
import no.nav.aap.fordeling.arkiv.fordeling.TestData.somMeldekort
import no.nav.aap.fordeling.arkiv.fordeling.TestData.utenBruker

class TestFordelingBeslutter {

    private val beslutter = FordelingBeslutter(mock(), mapper = ObjectMapper())

    @Test
    fun testBeslutter() {
        expect(JP, TIL_ARENA)
        expect(JP.medStatus(JOURNALFØRT), INGEN_FORDELING)
        expect(JP.somMeldekort(), INGEN_FORDELING)
        expect(JP.utenBruker(), TIL_GOSYS)
        expect(JP, INGEN_FORDELING, "JOURNALFOERT")
    }

    private fun expect(jp : Journalpost, status : FordelingsBeslutning, hendelsesStatus : String = "MOTTATT") =
        assertThat(beslutter.avgjørFordeling(jp, hendelsesStatus, "topic")).isEqualTo(status)
}