package no.nav.aap.fordeling.arkiv

import no.nav.aap.fordeling.arena.ArenaClient
import no.nav.aap.fordeling.egenansatt.EgenAnsattClient
import no.nav.aap.fordeling.navorganisasjon.NavOrgClient
import no.nav.aap.fordeling.oppgave.OppgaveClient
import no.nav.aap.fordeling.person.PDLClient
import org.springframework.stereotype.Component

@Component
data class Integrasjoner(val oppgave: OppgaveClient, val pdl: PDLClient, val org: NavOrgClient, val egen: EgenAnsattClient, val arena: ArenaClient, val arkiv: ArkivClient)