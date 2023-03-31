package no.nav.aap.fordeling.arkiv.fordeling

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.JournalpostDTO.Kanal.EESSI
import no.nav.aap.fordeling.arkiv.fordeling.TestData.JP
import no.nav.aap.fordeling.arkiv.fordeling.TestData.medKanal
import no.nav.aap.fordeling.arkiv.fordeling.TestData.somMeldekort

class TestFordelingBeslutter {

    @Test
    fun testBeslutter() {
        with(FordelingBeslutter()) {
            assertThat(skalFordele(JP)).isTrue()
            assertThat(skalFordele(JP.medKanal(EESSI))).isFalse()
            assertThat(skalFordele(JP.somMeldekort())).isFalse()
        }
        assertThat(FordelingBeslutter(FordelingConfig().copy(enabled = false)).skalFordele(JP)).isFalse()
    }
}