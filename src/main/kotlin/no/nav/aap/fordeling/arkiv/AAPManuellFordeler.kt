package no.nav.aap.fordeling.arkiv

import no.nav.aap.fordeling.arkiv.Fordeler.FordelingResultat
import no.nav.aap.util.Constants.AAP
import no.nav.aap.util.LoggerUtil
import org.springframework.stereotype.Component

@Component
class AAPManuellFordeler : ManuellFordeler{
    val log = LoggerUtil.getLogger(javaClass)

    override fun tema() = listOf(AAP)

    override fun fordel(journalpost: Journalpost): FordelingResultat {
        log.info("Fordeler manuelt $journalpost")
        return FordelingResultat("Manuell")
    }
}