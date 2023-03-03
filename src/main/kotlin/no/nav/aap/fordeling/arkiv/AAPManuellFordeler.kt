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
    val log = getLogger(javaClass)

    override fun tema() = listOf(AAP)

    override fun fordel(jp: Journalpost, enhet: NavEnhet) =
        with(integrasjoner)  {
            if (oppgave.harOppgave(jp.journalpostId)) {
                log.warn("Journalpost ${jp.journalpostId} har allerede journalføringsoppgave, avslutter manuell fordeling")
                FordelingResultat(jp.journalpostId,"Har allerede journalføringsoppgave")
            }
            else {
                runCatching {
                    log.info("Oppretter manuell journalføringsoppgave for ${jp.journalpostId}")
                    oppgave.opprettManuellJournalføringOppgave(jp,enhet)
                    FordelingResultat(jp.journalpostId,"Manuell journalføringsoppgave opprettet")
                }.getOrElse {
                    run {
                        log.warn("Opprettelse av manuell journalføringsopgave for ${jp.journalpostId} feilet, oppretter fordelingsoppgave", it)
                        oppgave.opprettFordelingOppgave(jp)
                        FordelingResultat(jp.journalpostId, "Manuell fordelingsoppgave oprettet")
                    }
                }
            }
        }
}