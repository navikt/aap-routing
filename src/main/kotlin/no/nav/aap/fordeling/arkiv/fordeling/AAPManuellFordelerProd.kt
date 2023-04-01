package no.nav.aap.fordeling.arkiv.fordeling

import org.springframework.stereotype.Component
import no.nav.aap.fordeling.arkiv.fordeling.Fordeler.FordelerConfig
import no.nav.aap.fordeling.arkiv.fordeling.Fordeler.FordelerConfig.Companion.PROD_AAP
import no.nav.aap.fordeling.navenhet.NAVEnhet
import no.nav.aap.fordeling.oppgave.OppgaveClient
import no.nav.aap.util.LoggerUtil.getLogger

@Component
class AAPManuellFordelerProd(private val oppgave : OppgaveClient, override val cfg : FordelerConfig = PROD_AAP) : AAPManuellFordeler(oppgave) {

    private val log = getLogger(AAPManuellFordelerProd::class.java)

    override fun opprettFordeling(jp : Journalpost) = log.info("Dummy oppretter fordelingsoppgave for journalpost ${jp.id}")

    override fun opprettJournalføring(jp : Journalpost, enhet : NAVEnhet) =
        log.info("Dummy oppretter journalføringsoppgave for journalpost ${jp.id}")

    override fun toString() = "AAPManuellFordelerProd(oppgave=$oppgave), cfg=$cfg)"
}