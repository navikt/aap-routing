package no.nav.aap.fordeling.arkiv

import no.nav.aap.fordeling.arkiv.Fordeler.FordelingResultat
import no.nav.aap.fordeling.arkiv.Tema.aap
import no.nav.aap.util.LoggerUtil
import org.springframework.stereotype.Component

@Component
class AAPManuellFordeler : ManuellFordeler{
    val log = LoggerUtil.getLogger(javaClass)

    override fun tema() = listOf(aap)

    override fun fordel(journalpost: Journalpost): FordelingResultat {
        log.info("Fordeler manuelt $journalpost")
        return FordelingResultat("Manuell")
    }
}