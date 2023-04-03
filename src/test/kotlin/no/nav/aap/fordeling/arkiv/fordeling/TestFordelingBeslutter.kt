package no.nav.aap.fordeling.arkiv.fordeling

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.JournalpostDTO.Kanal.EESSI
import no.nav.aap.fordeling.arkiv.fordeling.Journalpost.JournalpostStatus.JOURNALFØRT
import no.nav.aap.fordeling.arkiv.fordeling.TestData.JP
import no.nav.aap.fordeling.arkiv.fordeling.TestData.medKanal
import no.nav.aap.fordeling.arkiv.fordeling.TestData.medStatus
import no.nav.aap.fordeling.arkiv.fordeling.TestData.somMeldekort

class TestFordelingBeslutter {

    @Test
    fun testBeslutter() {
        with(FordelingBeslutter()) {
            assertThat(avgjørFordeling(JP)).isTrue()
            assertThat(avgjørFordeling(JP.medStatus(JOURNALFØRT))).isFalse()
            assertThat(avgjørFordeling(JP.medKanal(EESSI))).isFalse()
            assertThat(avgjørFordeling(JP.somMeldekort())).isFalse()
        }
        assertThat(FordelingBeslutter(FordelingConfig().copy(enabled = false)).avgjørFordeling(JP)).isFalse()
    }
}