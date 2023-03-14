package no.nav.aap.fordeling.arkiv.fordeling

import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.FordelingResultat
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.FordelingResultat.FordelingType.INGEN
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.FordelingResultat.FordelingType.MANUELL_FORDELING
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.FordelingResultat.FordelingType.MANUELL_JOURNALFØRING
import no.nav.aap.fordeling.navenhet.EnhetsKriteria.NavOrg.NAVEnhet
import no.nav.aap.fordeling.oppgave.OppgaveClient
import no.nav.aap.util.Constants.AAP
import no.nav.aap.util.LoggerUtil.getLogger
import org.springframework.stereotype.Component

@Component
class AAPManuellFordeler(private val oppgave: OppgaveClient) : ManuellFordeler {
    val log = getLogger(AAPManuellFordeler::class.java)

    override fun tema() = listOf(AAP)

    override fun fordel(jp: Journalpost, enhet: NAVEnhet) =
        with(jp) {
            if (oppgave.harOppgave(jp.journalpostId)) {
                FordelingResultat(INGEN,
                        journalpostId,
                        "Det finnes allerede en journalføringsoppgave for journalpost",
                        jp.hovedDokumentBrevkode)
            }
            else {
                runCatching {
                    log.info("Oppretter en manuell journalføringsoppgave for journalpost $journalpostId")
                    oppgave.opprettJournalføringOppgave(jp, enhet)
                    FordelingResultat(MANUELL_JOURNALFØRING, journalpostId, "Journalføringsoppgave opprettet", jp.hovedDokumentBrevkode)
                }.getOrElse {
                    runCatching {
                        log.warn("Feil ved opprettelse av en manuell journalføringsopgave for journalpost $journalpostId, oppretter fordelingsoppgave", it)
                        oppgave.opprettFordelingOppgave(jp)
                        FordelingResultat(MANUELL_FORDELING, journalpostId, "Fordelingsoppgave oprettet", jp.hovedDokumentBrevkode)
                    }.getOrElse {
                        log.warn("Feil ved opprettelse av en manuell fordelingsoppgave for journalpost $journalpostId")
                        throw ManuellFordelingException("Feil ved opprettelse av manuell fordelingsoppgave", it)
                    }
                }
            }
        }
}

class ManuellFordelingException(msg: String, cause: Throwable? = null) : RuntimeException(msg, cause)