package no.nav.aap.fordeling.oppgave

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

class OppgaveDTOs {

    data class OppgaveRespons(val antallTreffTotalt: Int, val oppgaver: List<Oppgave>) {
        @JsonIgnoreProperties(ignoreUnknown = true)
        data  class Oppgave(val id: Long)
    }
}