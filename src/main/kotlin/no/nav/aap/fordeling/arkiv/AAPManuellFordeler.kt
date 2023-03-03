package no.nav.aap.fordeling.arkiv

import no.nav.aap.fordeling.Integrasjoner
import no.nav.aap.fordeling.arkiv.Fordeler.FordelingResultat
import no.nav.aap.fordeling.arkiv.Fordeler.FordelingResultat.FordelingType.INGEN
import no.nav.aap.fordeling.arkiv.Fordeler.FordelingResultat.FordelingType.MANUELL_FORDELING
import no.nav.aap.fordeling.arkiv.Fordeler.FordelingResultat.FordelingType.MANUELL_JOURNALFØRING
import no.nav.aap.fordeling.navorganisasjon.EnhetsKriteria.NAVEnhet
import no.nav.aap.fordeling.oppgave.OppgaveClient
import no.nav.aap.util.Constants.AAP
import no.nav.aap.util.LoggerUtil.getLogger
import org.springframework.stereotype.Component

@Component
class AAPManuellFordeler(private val oppgave: OppgaveClient) : ManuellFordeler {
    val log = getLogger(AAPManuellFordeler::class.java)

    override fun tema() = listOf(AAP)

    override fun fordel(jp: Journalpost, enhet: NAVEnhet) =
        with(jp)  {
            if (oppgave.harOppgave(jp.journalpostId)) {
                FordelingResultat(journalpostId,"Det finnes allerede journalføringsoppgave for journalpost", INGEN)
            }
            else {
                runCatching {
                    log.info("Oppretter manuell journalføringsoppgave for journalpost $journalpostId")
                    oppgave.opprettManuellJournalføringOppgave(jp,enhet)
                    FordelingResultat(journalpostId,"Journalføringsoppgave opprettet",MANUELL_JOURNALFØRING)
                }.getOrElse {
                    runCatching {
                        log.warn("Feil ved opprettelse av manuell journalføringsopgave for journalpost $journalpostId, oppretter fordelingsoppgave", it)
                        oppgave.opprettFordelingOppgave(jp)
                        FordelingResultat(journalpostId, "Fordelingsoppgave oprettet",MANUELL_FORDELING)
                    }.getOrElse {
                        log.warn("Feil ved opprettelse av manuell fordelingsoppgave for journalpost $journalpostId")
                        throw ManuellException(journalpostId,"Feil ved opprettelse av manuell fordelingsoppgave",it)
                    }
                }
            }
        }
}
class ManuellException(val journalpostId: String, msg: String, cause: Throwable? = null) : RuntimeException(msg,cause)