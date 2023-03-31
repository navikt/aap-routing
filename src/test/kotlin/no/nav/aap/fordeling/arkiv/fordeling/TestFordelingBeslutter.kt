package no.nav.aap.fordeling.arkiv.fordeling

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.JournalpostDTO.JournalStatus.JOURNALFOERT
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.JournalpostDTO.Kanal.EESSI
import no.nav.aap.fordeling.arkiv.fordeling.TestData.JP
import no.nav.aap.fordeling.arkiv.fordeling.TestData.meldekort
import no.nav.aap.fordeling.arkiv.fordeling.TestData.withKanal
import no.nav.aap.fordeling.arkiv.fordeling.TestData.withStatus

class TestFordelingBeslutter {

    @Test
    fun testBeslutter() {
        with(FordelingBeslutter()) {
            assertThat(skalFordele(JP)).isTrue()
            assertThat(skalFordele(JP.withStatus(JOURNALFOERT))).isFalse()
            assertThat(skalFordele(JP.withKanal(EESSI))).isFalse()
            assertThat(skalFordele(JP.meldekort())).isFalse()
        }
        assertThat(FordelingBeslutter(FordelingConfig().copy(enabled = false)).skalFordele(JP)).isFalse()
    }
}