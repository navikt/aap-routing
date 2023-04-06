package no.nav.aap.fordeling.arkiv.fordeling

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import no.nav.aap.fordeling.arkiv.fordeling.DestinasjonUtvelger.Destinasjon
import no.nav.aap.fordeling.arkiv.fordeling.DestinasjonUtvelger.Destinasjon.ARENA
import no.nav.aap.fordeling.arkiv.fordeling.DestinasjonUtvelger.Destinasjon.GOSYS
import no.nav.aap.fordeling.arkiv.fordeling.DestinasjonUtvelger.Destinasjon.INGEN_DESTINASJON
import no.nav.aap.fordeling.arkiv.fordeling.Journalpost.JournalpostStatus
import no.nav.aap.fordeling.arkiv.fordeling.Journalpost.JournalpostStatus.JOURNALFØRT
import no.nav.aap.fordeling.arkiv.fordeling.Journalpost.JournalpostStatus.MOTTATT
import no.nav.aap.fordeling.arkiv.fordeling.TestData.JP
import no.nav.aap.fordeling.arkiv.fordeling.TestData.medStatus
import no.nav.aap.fordeling.arkiv.fordeling.TestData.somMeldekort
import no.nav.aap.fordeling.arkiv.fordeling.TestData.utenBruker

class TestDestinasjnnUtvelger {

    private val beslutter = DestinasjonUtvelger(ArenaBeslutter())

    @Test
    fun testBeslutter() {
        expect(JP, ARENA)
        expect(JP.medStatus(JOURNALFØRT), INGEN_DESTINASJON)
        expect(JP.somMeldekort(), INGEN_DESTINASJON)
        expect(JP.utenBruker(), GOSYS)
        expect(JP, INGEN_DESTINASJON, JOURNALFØRT)
    }

    private fun expect(jp : Journalpost, status : Destinasjon, hendelsesStatus : JournalpostStatus = MOTTATT) =
        assertThat(beslutter.destinasjon(jp, hendelsesStatus)).isEqualTo(status)
}