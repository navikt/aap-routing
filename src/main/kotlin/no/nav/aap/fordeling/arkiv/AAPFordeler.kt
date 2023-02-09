package no.nav.aap.fordeling.arkiv

import no.nav.aap.api.felles.error.IntegrationException
import no.nav.aap.fordeling.arkiv.Fordeler.FordelingResultat
import no.nav.aap.fordeling.arkiv.Tema.aap
import no.nav.aap.fordeling.navorganisasjon.NavEnhet
import no.nav.aap.util.LoggerUtil
import org.springframework.stereotype.Component

@Component
class AAPFordeler(private val integrator: Integrator) : Fordeler {

    private val log = LoggerUtil.getLogger(javaClass)
    override fun tema() = listOf(aap)
    override fun fordel(journalpost: Journalpost): FordelingResultat {
           integrator.slåOpp(journalpost).also {
               log.info("Slo opp $it")
           }
        log.info("Fordeler $journalpost")  // TOOO gammel krut logikk
        throw IntegrationException("TESTING 123")
        //return FordelingResultat("OK")
    }
}
@Component
class Integrator(private val integrasjoner: Integrasjoner) {
    fun slåOpp(jp: Journalpost) =
        runCatching {
            with(integrasjoner) {
                OppslagResultat(jp, org.navEnhet(pdl.geoTilknytning(jp.fnr), egen.erSkjermet(jp.fnr), pdl.diskresjonskode(jp.fnr)))
            }
        }.getOrThrow()
}
data class OppslagResultat(val journalpost: Journalpost, val enhet: NavEnhet)