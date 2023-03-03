package no.nav.aap.fordeling.arkiv

import no.nav.aap.fordeling.Integrasjoner
import no.nav.aap.fordeling.arkiv.Fordeler.FordelingResultat
import no.nav.aap.fordeling.navorganisasjon.EnhetsKriteria.NavEnhet
import no.nav.aap.util.Constants.AAP
import no.nav.aap.util.LoggerUtil.getLogger
import org.springframework.core.env.Environment
import org.springframework.stereotype.Component

@Component
class AAPManuellFordeler(private val integrasjoner: Integrasjoner, env: Environment) : ManuellFordeler {
    val log = getLogger(AAPManuellFordeler::class.java)

    override fun tema() = listOf(AAP)

    override fun fordel(jp: Journalpost, enhet: NavEnhet) =
        with(integrasjoner)  {
            if (oppgave.harOppgave(jp.journalpostId)) {
                FordelingResultat(jp.journalpostId,"Har allerede journalføringsoppgave").also {
                    log.info("${it.journalpostId} ${it.msg}")
                }
            }
            else {
                runCatching {
                    log.info("Oppretter manuell journalføringsoppgave for journalpost ${jp.journalpostId}")
                    oppgave.opprettManuellJournalføringOppgave(jp,enhet)
                    FordelingResultat(jp.journalpostId,"Manuell journalføringsoppgave opprettet").also {
                        log.info("${it.msg} for ${it.journalpostId}")
                    }
                }.getOrElse {
                    runCatching {
                        log.warn("Opprettelse av manuell journalføringsopgave for journalpost ${jp.journalpostId} feilet, oppretter fordelingsoppgave", it)
                        oppgave.opprettFordelingOppgave(jp)
                        FordelingResultat(jp.journalpostId, "Manuell fordelingsoppgave oprettet").also {
                            log.info("${it.msg} for ${it.journalpostId}")
                        }
                    }.getOrElse {
                        log.warn("Journalpost ${jp.journalpostId} feilet ved opprettelse av manuell fordelingsoppgave")
                        throw ManuellException(jp.journalpostId,"Feil ved opprettelse av manuell fordelingsoppgave",it)
                    }
                }
            }
        }
}
class ManuellException(val journalpostId: String, msg: String, cause: Throwable? = null) : RuntimeException(msg,cause)