package no.nav.aap.fordeling.arkiv.fordeling

import org.springframework.stereotype.Component
import no.nav.aap.fordeling.arkiv.fordeling.Fordeler.FordelingResultat
import no.nav.aap.fordeling.arkiv.fordeling.Fordeler.FordelingResultat.FordelingType.ALLEREDE_OPPGAVE
import no.nav.aap.fordeling.arkiv.fordeling.Fordeler.FordelingResultat.FordelingType.MANUELL_FORDELING
import no.nav.aap.fordeling.arkiv.fordeling.Fordeler.FordelingResultat.FordelingType.MANUELL_JOURNALFØRING
import no.nav.aap.fordeling.navenhet.NAVEnhet
import no.nav.aap.fordeling.oppgave.OppgaveClient
import no.nav.aap.util.LoggerUtil.getLogger
import no.nav.boot.conditionals.Cluster.Companion.isProd

@Component
class AAPManuellFordeler(private val oppgave : OppgaveClient) : ManuellFordeler {

    private val log = getLogger(AAPManuellFordeler::class.java)

    override fun fordelManuelt(jp : Journalpost, enhet : NAVEnhet?) = fordel(jp, enhet)

    override fun fordel(jp : Journalpost, enhet : NAVEnhet?) =
        enhet?.let {
            with(jp) {
                if (oppgave.harOppgave(id)) {
                    log.info("Det finnes allerede en journalføringsoppgave for journalpost ${jp.id}")
                    FordelingResultat(ALLEREDE_OPPGAVE, "Det finnes allerede en journalføringsoppgave, oppretter ingen ny", hovedDokumentBrevkode, id, enhet)
                }
                else {
                    runCatching {
                        opprettJournalføringsOppgave(jp, it)
                    }.getOrElse {
                        log.warn("Opprettelse av journalføringsoppgave feilet", it)
                        opprettFordelingsOppgave(jp)
                    }
                }
            }
        } ?: opprettFordelingsOppgave(jp)

    private fun opprettJournalføringsOppgave(jp : Journalpost, enhet : NAVEnhet) =
        with(jp) {
            opprettJournalføring(this, enhet)
            FordelingResultat(MANUELL_JOURNALFØRING, "Journalføringsoppgave opprettet", hovedDokumentBrevkode, id, enhet)
        }

    private fun opprettFordelingsOppgave(jp : Journalpost) =
        with(jp) {
            runCatching {
                opprettFordeling(this)
                FordelingResultat(MANUELL_FORDELING, "Fordelingsoppgave opprettet", hovedDokumentBrevkode, id)
            }.getOrElse {
                with("Feil ved opprettelse av en manuell fordelingsoppgave for journalpost $id") {
                    log.warn(this)
                    throw ManuellFordelingException(this, it)
                }
            }
        }

    protected fun opprettFordeling(jp : Journalpost) {
        if (isProd()) {
            log.info("Ingen opprettelse av fordelingsoppgave i prod foreløpig")
        }
        else {
            log.info("Oppretter fordelingsoppgave for ${jp.id}")
            oppgave.opprettFordelingOppgave(jp).also {
                log.info("Opprettet fordelingsoppgave for ${jp.id}")
            }
        }
    }

    protected fun opprettJournalføring(jp : Journalpost, enhet : NAVEnhet) {
        if (isProd()) {
            log.info("Ingen opprettelse av journalføringsoppgave i prod foreløpig")
        }
        else {
            log.info("Oppretter en journalføringsoppgave for journalpost ${jp.id}")
            oppgave.opprettJournalføringOppgave(jp, enhet).also {
                log.info("Opprettet journalføringsoppgave for ${jp.id}")
            }
        }
    }

    override fun toString() = "AAPManuellFordeler(oppgave=$oppgave)"
}