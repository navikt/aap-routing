package no.nav.aap.fordeling.arkiv

import no.nav.aap.fordeling.Integrasjoner
import no.nav.aap.fordeling.arkiv.Fordeler.FordelingResultat
import no.nav.aap.fordeling.navorganisasjon.EnhetsKriteria.NavEnhet
import no.nav.aap.util.Constants.AAP
import no.nav.aap.util.LoggerUtil.getLogger
import org.springframework.stereotype.Component

@Component
class AAPManuellFordeler(private val integrasjoner: Integrasjoner) : ManuellFordeler{
    val log = getLogger(javaClass)

    override fun tema() = listOf(AAP)

    override fun fordel(journalpost: Journalpost, enhet: NavEnhet): FordelingResultat =
        with(integrasjoner)  {
            if (oppgave.harOppgave(journalpost.journalpostId)) {
                log.info("Journalpost ${journalpost.journalpostId} har allerede en oppgave, avslutter manuell fordeling")
            }
            else {
                runCatching {
                    oppgave.opprettManuellJournalføringOppgave(journalpost,enhet)
                }.getOrElse {
                    oppgave.opprettFordelingOppgave(journalpost)  // TODO hva hvis denne feiler, fjerne id ???
                }
            }
             FordelingResultat(msg ="Manuell")
        }
}