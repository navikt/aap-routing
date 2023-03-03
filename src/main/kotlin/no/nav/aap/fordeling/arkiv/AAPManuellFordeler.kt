package no.nav.aap.fordeling.arkiv

import no.nav.aap.fordeling.Integrasjoner
import no.nav.aap.fordeling.arkiv.Fordeler.FordelingResultat
import no.nav.aap.fordeling.navorganisasjon.EnhetsKriteria.NavEnhet
import no.nav.aap.util.Constants.AAP
import no.nav.aap.util.LoggerUtil.getLogger
import org.springframework.stereotype.Component

@Component
class AAPManuellFordeler(private val integrasjoner: Integrasjoner) : ManuellFordeler {
    val log = getLogger(javaClass)

    override fun tema() = listOf(AAP)

    override fun fordel(journalpost: Journalpost, enhet: NavEnhet): FordelingResultat =
        with(integrasjoner)  {
            if (oppgave.harOppgave(journalpost.journalpostId)) {
                log.warn("Journalpost ${journalpost.journalpostId} har allerede en oppgave, avslutter manuell fordeling")
            }
            else {
                runCatching {
                    log.info("Oppretter manuell journalføringsoppgave for $journalpost")
                    oppgave.opprettManuellJournalføringOppgave(journalpost,enhet)
                }.getOrElse {
                    runCatching {
                        log.warn("Opprettelse av manuell journalføringsopgave for $journalpost feilet, prøver fordelingsoppgave",it)
                        oppgave.opprettFordelingOppgave(journalpost)
                    }.getOrThrow()
                }
            }
            FordelingResultat(msg ="Manuell")
        }
}