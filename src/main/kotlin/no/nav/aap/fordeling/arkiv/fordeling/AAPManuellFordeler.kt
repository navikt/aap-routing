package no.nav.aap.fordeling.arkiv.fordeling

import no.nav.aap.fordeling.arkiv.fordeling.FordelerConfig.Companion.DEV_AAP
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.FordelingResultat
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.FordelingResultat.FordelingType.INGEN
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.FordelingResultat.FordelingType.MANUELL_FORDELING
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.FordelingResultat.FordelingType.MANUELL_JOURNALFØRING
import no.nav.aap.fordeling.navenhet.EnhetsKriteria.NavOrg.NAVEnhet
import no.nav.aap.fordeling.oppgave.OppgaveClient
import no.nav.aap.util.LoggerUtil.getLogger
import org.springframework.stereotype.Component

@Component
open class AAPManuellFordeler(private val oppgave: OppgaveClient) : ManuellFordeler {
    val log = getLogger(AAPManuellFordeler::class.java)
    override val cfg = DEV_AAP // For NOW
    override fun fordelManuelt(jp: Journalpost, enhet: NAVEnhet?) = fordel(jp,enhet)
    override fun fordel(jp: Journalpost, enhet: NAVEnhet?) =
        enhet?.let {
            with(jp) {
                if (oppgave.harOppgave(journalpostId)) {
                    with("Det finnes allerede en journalføringsoppgave, oppretter ingen ny") {
                        FordelingResultat(INGEN, this, hovedDokumentBrevkode, journalpostId).also {
                            log.info(it.msg())
                        }
                    }
                }
                else {
                    runCatching {
                        journalføringsOppgave(jp,it)
                    }.getOrElse {
                        fordelingsOppgave(jp)
                    }
                }
            }
        }?: fordelingsOppgave(jp)

    private fun journalføringsOppgave(jp: Journalpost, enhet: NAVEnhet) =
        with(jp) {
            log.info("Oppretter en manuell journalføringsoppgave for journalpost $journalpostId")
           opprettJournalføring(this, enhet)
            with("Journalføringsoppgave opprettet")  {
                FordelingResultat(MANUELL_JOURNALFØRING, this, hovedDokumentBrevkode, journalpostId).also {
                    log.info(it.msg())
                }
            }
        }

    private fun fordelingsOppgave(jp: Journalpost) =
        with(jp) {
            runCatching {
                log.info("Oppretter fordelingsoppgave for $journalpostId")
                opprettFordeling(this)
                with("Fordelingsoppgave opprettet")  {
                    FordelingResultat(MANUELL_FORDELING, this, hovedDokumentBrevkode, journalpostId).also {
                    }}
            }.getOrElse {
                with("Feil ved opprettelse av en manuell fordelingsoppgave for journalpost $journalpostId") {
                    log.warn(this)
                    throw ManuellFordelingException(this, it)
                }
            }
        }
    protected fun opprettFordeling(jp: Journalpost) = oppgave.opprettFordelingOppgave(jp)
    protected fun opprettJournalføring(jp: Journalpost, enhet: NAVEnhet) = oppgave.opprettJournalføringOppgave(jp,enhet)
    override fun toString() = "AAPManuellFordeler(oppgave=$oppgave, cfg=$cfg)"
}