package no.nav.aap.fordeling.arkiv.fordeling

import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.FordelingResultat
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.FordelingResultat.FordelingType.INGEN
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.FordelingResultat.FordelingType.MANUELL_FORDELING
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.FordelingResultat.FordelingType.MANUELL_JOURNALFØRING
import no.nav.aap.fordeling.navenhet.EnhetsKriteria
import no.nav.aap.fordeling.navenhet.EnhetsKriteria.NavOrg.NAVEnhet
import no.nav.aap.fordeling.oppgave.OppgaveClient
import no.nav.aap.util.LoggerUtil.getLogger
import no.nav.boot.conditionals.ConditionalOnNotProd
import org.springframework.stereotype.Component

@Component
@ConditionalOnNotProd
open class AAPManuellFordeler(private val oppgave: OppgaveClient) : ManuellFordeler {
    val log = getLogger(AAPManuellFordeler::class.java)

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

    protected fun journalføringsOppgave(jp: Journalpost, enhet: NAVEnhet) =
        with(jp) {
            log.info("Oppretter en manuell journalføringsoppgave for journalpost $journalpostId")
            oppgave.opprettJournalføringOppgave(this, enhet)
            with("Journalføringsoppgave opprettet")  {
                FordelingResultat(MANUELL_JOURNALFØRING, this, hovedDokumentBrevkode, journalpostId).also {
                    log.info(it.msg())
                }
            }
        }

    protected fun fordelingsOppgave(jp: Journalpost) =
        with(jp) {
            runCatching {
                log.info("Oppretter fordelingsoppgave for $journalpostId")
                oppgave.opprettFordelingOppgave(this)
                with("Fordelingsoppgave opprettet")  {
                    FordelingResultat(MANUELL_FORDELING, this, hovedDokumentBrevkode, journalpostId).also {
                        log.info(it.msg())
                    }}
            }.getOrElse {
                with("Feil ved opprettelse av en manuell fordelingsoppgave for journalpost $journalpostId") {
                    log.warn(this)
                    throw ManuellFordelingException(this, it)
                }
            }
        }
}