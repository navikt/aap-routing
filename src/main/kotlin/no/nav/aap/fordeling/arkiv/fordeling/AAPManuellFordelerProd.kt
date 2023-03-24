package no.nav.aap.fordeling.arkiv.fordeling

import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.FordelingResultat
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.FordelingResultat.FordelingType.INGEN
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.FordelingResultat.FordelingType.MANUELL_FORDELING
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.FordelingResultat.FordelingType.MANUELL_JOURNALFØRING
import no.nav.aap.fordeling.navenhet.EnhetsKriteria.NavOrg.NAVEnhet
import no.nav.aap.fordeling.oppgave.OppgaveClient
import no.nav.aap.util.LoggerUtil.getLogger
import no.nav.boot.conditionals.ConditionalOnProd
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Component

@Component
@Primary
@ConditionalOnProd
class AAPManuellFordelerProd(private val oppgave: OppgaveClient) : AAPManuellFordeler(oppgave) {
    override val log = getLogger(AAPManuellFordelerProd::class.java)
    override fun opprettFordeling(jp: Journalpost) = log.info("Liksomoppretter fordelingsoppgave")
    override fun opprettJournalføring(jp: Journalpost, enhet: NAVEnhet) =  log.info("Liksomoppretter journalføringsoppgave")
}