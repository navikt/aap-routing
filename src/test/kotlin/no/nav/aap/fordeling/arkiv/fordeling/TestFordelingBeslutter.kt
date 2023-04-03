package no.nav.aap.fordeling.arkiv.fordeling

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import no.nav.aap.fordeling.arkiv.fordeling.FordelingBeslutter.BeslutningsStatus
import no.nav.aap.fordeling.arkiv.fordeling.FordelingBeslutter.BeslutningsStatus.INGEN_FORDELING
import no.nav.aap.fordeling.arkiv.fordeling.FordelingBeslutter.BeslutningsStatus.TIL_ARENA_FORDELING
import no.nav.aap.fordeling.arkiv.fordeling.FordelingBeslutter.BeslutningsStatus.TIL_MANUELL_ARENA_FORDELING
import no.nav.aap.fordeling.arkiv.fordeling.Journalpost.JournalpostStatus.JOURNALFØRT
import no.nav.aap.fordeling.arkiv.fordeling.TestData.JP
import no.nav.aap.fordeling.arkiv.fordeling.TestData.medStatus
import no.nav.aap.fordeling.arkiv.fordeling.TestData.somMeldekort
import no.nav.aap.fordeling.arkiv.fordeling.TestData.utenBruker

class TestFordelingBeslutter {

    private val beslutter = FordelingBeslutter(mock())

    @Test
    fun testBeslutter() {
        expect(JP, TIL_ARENA_FORDELING)
        expect(JP.medStatus(JOURNALFØRT), INGEN_FORDELING)
        expect(JP.somMeldekort(), INGEN_FORDELING)
        expect(JP.utenBruker(), TIL_MANUELL_ARENA_FORDELING)
        expect(JP, INGEN_FORDELING, "JOURNALFOERT")
    }

    private fun expect(jp : Journalpost, status : BeslutningsStatus, hendelsesStatus : String = "MOTTATT") =
        assertThat(beslutter.avgjørFordeling(jp, hendelsesStatus, "topic")).isEqualTo(status)
}