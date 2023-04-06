package no.nav.aap.fordeling.arkiv.fordeling

import org.springframework.stereotype.Component
import no.nav.aap.fordeling.arkiv.fordeling.DestinasjonUtvelger.Destinasjon.ARENA
import no.nav.aap.fordeling.arkiv.fordeling.DestinasjonUtvelger.Destinasjon.GOSYS
import no.nav.aap.fordeling.arkiv.fordeling.DestinasjonUtvelger.Destinasjon.INGEN_DESTINASJON
import no.nav.aap.fordeling.arkiv.fordeling.DestinasjonUtvelger.Destinasjon.KELVIN
import no.nav.aap.fordeling.arkiv.fordeling.Fordeler.FordelingResultat.FordelingType.DIREKTE_MANUELL
import no.nav.aap.fordeling.arkiv.fordeling.Journalpost.JournalpostStatus
import no.nav.aap.fordeling.navenhet.NAVEnhet.Companion.FORDELINGSENHET
import no.nav.aap.fordeling.navenhet.NavEnhetUtvelger
import no.nav.aap.fordeling.slack.Slacker
import no.nav.aap.util.LoggerUtil

@Component
class DestinasjonFordeler(private val utvelger : DestinasjonUtvelger, private val arenaFordeler : FordelingFactory, private val enhet : NavEnhetUtvelger,
                          private val slack : Slacker) {

    private val log = LoggerUtil.getLogger(DestinasjonFordeler::class.java)

    fun fordel(jp : Journalpost, status : JournalpostStatus) {

        when (utvelger.destinasjon(jp, status)) {

            KELVIN -> log.warn("Fordeling til Kelvin ikke implementert")

            INGEN_DESTINASJON -> {
                log.info("Ingen fordeling av journalpost ${jp.id}, forutsetninger for fordeling ikke oppfylt")
            }

            GOSYS -> {
                arenaFordeler.fordelManuelt(jp, FORDELINGSENHET)
                jp.metrikker(DIREKTE_MANUELL)
            }

            ARENA -> {
                log.info("Begynner fordeling av ${jp.id} (behandlingstema='${jp.behandlingstema}', tittel='${jp.tittel}', brevkode='${jp.hovedDokumentBrevkode}', status='${jp.status}')")
                fordelArena(jp)
            }
        }
    }

    private fun fordelArena(jp : Journalpost) =
        arenaFordeler.fordel(jp, enhet.navEnhet(jp)).also {
            slack.meldingHvisDev("$it (${jp.fnr})")
            log.info("$it")
            jp.metrikker(it.fordelingstype)
        }
}