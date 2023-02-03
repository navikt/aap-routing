package no.nav.aap.routing.person

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import no.nav.aap.routing.person.PDLAdressebeskyttelse.PDLGradering
import no.nav.aap.routing.person.PDLAdressebeskyttelse.PDLGradering.PDLDiskresjonskode
import org.junit.jupiter.api.Test

class PDLTest {





    @Test
    fun testGradering(){

        val obj = PDLAdressebeskyttelse(listOf(PDLGradering(PDLDiskresjonskode.STRENGT_FORTROLIG)))
        println(ObjectMapper().writeValueAsString(obj))
        val m = ObjectMapper().registerModule(KotlinModule())

        val json = """
             {"adressebeskyttelse":[{"gradering":"STRENGT_FORTROLIG"}]}
        """.trimIndent()
          val o = m.readValue(json,PDLAdressebeskyttelse::class.java)
        println(o)
    }


}