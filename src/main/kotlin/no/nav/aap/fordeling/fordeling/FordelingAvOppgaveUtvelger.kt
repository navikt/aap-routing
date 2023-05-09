package no.nav.aap.fordeling.fordeling

import no.nav.aap.fordeling.arkiv.journalpost.Journalpost
import org.springframework.stereotype.Component
import no.nav.aap.fordeling.fordeling.Fordeler.FordelingResultat.FordelingType.ALLEREDE_JOURNALFØRT
import no.nav.aap.fordeling.fordeling.Fordeler.FordelingResultat.FordelingType.INGEN
import no.nav.aap.fordeling.fordeling.Fordeler.FordelingResultat.FordelingType.RACE
import no.nav.aap.fordeling.arkiv.journalpost.Journalpost.JournalpostStatus
import no.nav.aap.fordeling.arkiv.journalpost.Journalpost.JournalpostStatus.JOURNALFØRT
import no.nav.aap.fordeling.fordeling.FordelingAvOppgaveUtvelger.FordelingsBeslutning.GOSYS
import no.nav.aap.fordeling.fordeling.FordelingAvOppgaveUtvelger.FordelingsBeslutning.INGEN_DESTINASJON
import no.nav.aap.util.LoggerUtil

@Component
class FordelingAvOppgaveUtvelger(private val beslutter : Beslutter, private val cfg : FordelingConfig = FordelingConfig()) {

    private val log = LoggerUtil.getLogger(FordelingAvOppgaveUtvelger::class.java)

    enum class FordelingsBeslutning { KELVIN, ARENA, INGEN_DESTINASJON, GOSYS }

    fun destinasjon(jp : Journalpost, status : JournalpostStatus, topic : String) : FordelingsBeslutning {

        if (!cfg.isEnabled) {
            return ingen(jp, topic, "Fordeling er ikke aktivert")
        }

        if (jp.erMeldekort) {
            return ingen(jp, topic, "Meldekort håndteres av andre")
        }

        if (jp.status == JOURNALFØRT) {
            return INGEN_DESTINASJON.also {
                log.info(txt(jp, "Allerede journalført"))
                jp.metrikker(ALLEREDE_JOURNALFØRT, topic)
            }
        }

        if (jp.bruker == null) {
            return GOSYS.also {
                log.warn("Ingen bruker er satt på journalposten, sender direkte til manuell journalføring")
            }
        }

        if (jp.status != status) {
            return INGEN_DESTINASJON.also {
                log.warn(txt(jp, "race condition, status endret fra $status til ${jp.status}, sjekk om noen andre ferdigstiller"))
                jp.metrikker(RACE, topic)
            }
        }

        return beslutter.beslutt(jp)
    }

    private fun txt(jp : Journalpost, txt : String) = "Journalpost ${jp.id} $txt (tittel='${jp.tittel}', brevkode='${jp.hovedDokumentBrevkode}')"

    private fun ingen(jp : Journalpost, topic : String, ekstra : String = "") =
        INGEN_DESTINASJON.also {
            log.info("Journalpost ${jp.id} med status '${jp.status}' fra kanal '${jp.kanal}' skal IKKE fordeles (tittel='${jp.tittel}', brevkode='${jp.hovedDokumentBrevkode}'). $ekstra")
            jp.metrikker(INGEN, topic)
        }

    override fun toString() = "DefaultFordelingBeslutter(cfg=$cfg)"
}