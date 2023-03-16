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

    override fun fordel(jp: Journalpost, enhet: NAVEnhet) =
        with(jp) {
            if (oppgave.harOppgave(jp.journalpostId)) {
                with("Det finnes allerede en journalføringsoppgave, oppretter ingen ny") {
                    FordelingResultat(INGEN, this, jp.hovedDokumentBrevkode, journalpostId).also {
                        log.info(it.msg())
                    }
                }
            }
            else {
                runCatching {
                    log.info("Oppretter en manuell journalføringsoppgave for journalpost $journalpostId")
                    oppgave.opprettJournalføringOppgave(jp, enhet)
                    with("Journalføringsoppgave opprettet")  {
                        FordelingResultat(MANUELL_JOURNALFØRING, this, jp.hovedDokumentBrevkode, journalpostId).also {
                            log.info(it.msg())
                        }
                    }
                }.getOrElse {
                    runCatching {
                        log.warn("Feil ved opprettelse av en manuell journalføringsopgave for journalpost $journalpostId, oppretter fordelingsoppgave", it)
                        oppgave.opprettFordelingOppgave(jp)
                        with("Fordelingsoppgave oprettet")  {
                            FordelingResultat(MANUELL_FORDELING, this, jp.hovedDokumentBrevkode, journalpostId).also {
                                log.info(it.msg())
                            }
                        }
                    }.getOrElse {
                        with("Feil ved opprettelse av en manuell fordelingsoppgave for journalpost $journalpostId") {
                            log.warn(this)
                            throw ManuellFordelingException(this, it)
                        }
                    }
                }
            }
        }
}

class ManuellFordelingException(msg: String, cause: Throwable? = null) : RuntimeException(msg, cause)