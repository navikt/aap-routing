package no.nav.aap.fordeling.arkiv

import no.nav.aap.fordeling.Integrasjoner
import no.nav.aap.fordeling.arkiv.Fordeler.FordelingResultat
import no.nav.aap.fordeling.arkiv.Fordeler.FordelingResultat.FordelingType.INGEN
import no.nav.aap.fordeling.arkiv.Fordeler.FordelingResultat.FordelingType.MANUELL_FORDELING
import no.nav.aap.fordeling.arkiv.Fordeler.FordelingResultat.FordelingType.MANUELL_JOURNALFØRING
import no.nav.aap.fordeling.navorganisasjon.EnhetsKriteria.NAVEnhet
import no.nav.aap.util.Constants.AAP
import no.nav.aap.util.LoggerUtil.getLogger
import org.springframework.stereotype.Component

@Component
class AAPManuellFordeler(private val integrasjoner: Integrasjoner) : ManuellFordeler {
    val log = getLogger(AAPManuellFordeler::class.java)

    override fun tema() = listOf(AAP)

    override fun fordel(jp: Journalpost, enhet: NAVEnhet) =
        with(integrasjoner)  {
            if (oppgave.harOppgave(jp.journalpostId)) {
                FordelingResultat(jp.journalpostId,"Har allerede journalføringsoppgave", INGEN)
            }
            else {
                runCatching {
                    log.info("Oppretter manuell journalføringsoppgave for journalpost ${jp.journalpostId}")
                    oppgave.opprettManuellJournalføringOppgave(jp,enhet)
                    FordelingResultat(jp.journalpostId,"journalføringsoppgave opprettet",MANUELL_JOURNALFØRING)
                }.getOrElse {
                    runCatching {
                        log.warn("Opprettelse av manuell journalføringsopgave for journalpost ${jp.journalpostId} feilet, oppretter fordelingsoppgave", it)
                        oppgave.opprettFordelingOppgave(jp)
                        FordelingResultat(jp.journalpostId, "fordelingsoppgave oprettet",MANUELL_FORDELING)
                    }.getOrElse {
                        log.warn("Journalpost ${jp.journalpostId} feilet ved opprettelse av manuell fordelingsoppgave")
                        throw ManuellException(jp.journalpostId,"Feil ved opprettelse av manuell fordelingsoppgave",it)
                    }
                }
            }
        }
}
class ManuellException(val journalpostId: String, msg: String, cause: Throwable? = null) : RuntimeException(msg,cause)