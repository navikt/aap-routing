package no.nav.aap.fordeling.arkiv.fordeling

import no.nav.aap.fordeling.navenhet.EnhetsKriteria.NavOrg.NAVEnhet
import no.nav.aap.fordeling.oppgave.OppgaveClient
import no.nav.aap.util.Constants.AAP
import no.nav.aap.util.LoggerUtil.getLogger
import no.nav.boot.conditionals.Cluster.Companion.prodClusters
import org.springframework.stereotype.Component

@Component
class AAPManuellFordelerProd(private val oppgave: OppgaveClient) : AAPManuellFordeler(oppgave) {
    override val log = getLogger(AAPManuellFordelerProd::class.java)

    override fun tema() = listOf(AAP)
    override fun clusters() = prodClusters()

    override fun opprettFordeling(jp: Journalpost) = log.info("Liksom oppretter fordelingsoppgave")
    override fun opprettJournalføring(jp: Journalpost, enhet: NAVEnhet) =  log.info("Liksom oppretter journalføringsoppgave")
    override fun toString() = "AAPManuellFordelerProd(oppgave=$oppgave), tema=${tema()}, clusters=${clusters().asList()}"
}