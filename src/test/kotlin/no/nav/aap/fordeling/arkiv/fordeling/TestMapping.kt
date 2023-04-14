package no.nav.aap.fordeling.arkiv.fordeling

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import org.mockito.Mockito.*
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.slf4j.MDC
import no.nav.aap.fordeling.arkiv.fordeling.Fordeler.Companion.FIKTIVTFNR
import no.nav.aap.fordeling.arkiv.fordeling.TestData.AKTØR
import no.nav.aap.fordeling.arkiv.fordeling.TestData.DTO
import no.nav.aap.fordeling.egenansatt.EgenAnsattClient
import no.nav.aap.fordeling.person.Diskresjonskode.ANY
import no.nav.aap.fordeling.person.PDLClient
import no.nav.aap.util.MDCUtil

@TestInstance(PER_CLASS)
class TestMapping {

    val pdl : PDLClient = mock()
    val egen : EgenAnsattClient = mock()

    @Test
    @DisplayName("Mapper skal veksle inn aktørId, sette egen ansatt")
    fun mapOK() {
        whenever(pdl.fnr(AKTØR)).thenReturn(FIKTIVTFNR)
        whenever(pdl.diskresjonskode(FIKTIVTFNR)).thenReturn(ANY)
        whenever(egen.erEgenAnsatt(FIKTIVTFNR)).thenReturn(true)
        val jp = JournalpostMapper(pdl, egen).tilJournalpost(DTO)
        verify(egen).erEgenAnsatt(FIKTIVTFNR)
        verify(pdl).fnr(AKTØR)
        assertEquals(jp.egenAnsatt, true)
        assertEquals(jp.fnr.fnr, DTO.avsenderMottaker?.id)
    }

    val url = "https://logs.adeo.no/app/kibana#/discover" +
        "?_g=(" +
        "refreshInterval:(pause:!t,value:0)," +
        "time:(from:now-15m,to:now))" +
        "&_a=(" +
        "columns:!(level,message,envclass,application,pod)" +
        "index:'logstash-*'," +
        "interval:auto," +
        "query:(language:kuery," +
        "query:'%%22${MDC.get(MDCUtil.NAV_CALL_ID)}%%22')," +
        "sort:!(!('@timestamp',desc))" +
        ")"

    val u1 = "https://logs.adeo.no/s/nav-logs-legacy/app/discover#/" +
        "?_g=(" +
        "filters:!()," +
        "refreshInterval:(pause:!t,value:60000)," +
        "time:(from:now-15h,to:now))" +
        "&_a=(" +
        "columns:!(level,message,envclass,application,pod)," +
        "filters:!()," +
        "interval:auto," +
        "query:(language:kuery," +
        "query:%22" + MDCUtil.callId() + "%22)," +
        "sort:!(!('@timestamp',desc)))"

    @Test
    fun doit() {
        MDCUtil.callId()
        println(u1)
    }
}