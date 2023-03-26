package no.nav.aap.fordeling.arkiv.fordeling

import no.nav.aap.fordeling.arkiv.fordeling.FordelerConfig.Companion.PROD_AAP
import no.nav.aap.fordeling.navenhet.EnhetsKriteria.NavOrg.NAVEnhet
import no.nav.aap.fordeling.oppgave.OppgaveClient
import no.nav.aap.util.LoggerUtil.getLogger
import org.springframework.stereotype.Component

@Component
class AAPManuellFordelerProd(private val oppgave: OppgaveClient) : AAPManuellFordeler(oppgave) {
    override val log = getLogger(AAPManuellFordelerProd::class.java)
    override val cfg = PROD_AAP
    override fun opprettFordeling(jp: Journalpost) = log.info("Liksom oppretter fordelingsoppgave for journalpost ${jp.journalpostId}")
    override fun opprettJournalføring(jp: Journalpost, enhet: NAVEnhet) =  log.info("Liksom oppretter journalføringsoppgave for journalpost ${jp.journalpostId}")
    override fun toString() = "AAPManuellFordelerProd(oppgave=$oppgave), cfg=$cfg)"
}