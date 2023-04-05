package no.nav.aap.fordeling.arkiv.fordeling

import org.springframework.stereotype.Component
import no.nav.aap.fordeling.arkiv.fordeling.DestinasjonUtvelger.Destinasjon.GOSYS
import no.nav.aap.fordeling.arkiv.fordeling.DestinasjonUtvelger.Destinasjon.INGEN_DESTINASJON
import no.nav.aap.fordeling.arkiv.fordeling.Fordeler.FordelingResultat.FordelingType.ALLEREDE_JOURNALFØRT
import no.nav.aap.fordeling.arkiv.fordeling.Fordeler.FordelingResultat.FordelingType.INGEN
import no.nav.aap.fordeling.arkiv.fordeling.Fordeler.FordelingResultat.FordelingType.RACE
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.JournalpostDTO.Kanal.EESSI
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.JournalpostDTO.Kanal.EKST_OPPS
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.JournalpostDTO.Kanal.NAV_NO_CHAT
import no.nav.aap.fordeling.arkiv.fordeling.Journalpost.JournalpostStatus
import no.nav.aap.fordeling.arkiv.fordeling.Journalpost.JournalpostStatus.JOURNALFØRT
import no.nav.aap.util.LoggerUtil

@Component
class DestinasjonUtvelger(private val beslutter : Beslutter, private val cfg : FordelingConfig = FordelingConfig()) {

    private val log = LoggerUtil.getLogger(DestinasjonUtvelger::class.java)

    enum class Destinasjon { KELVIN, ARENA, INGEN_DESTINASJON, GOSYS }

    fun destinasjon(jp : Journalpost, status : JournalpostStatus) : Destinasjon {

        if (!cfg.isEnabled) {
            return ingen(jp, "Fordeling er ikke aktivert")
        }

        if (jp.erMeldekort()) {
            return ingen(jp, "Meldekort håndteres av andre")
        }

        if (jp.status == JOURNALFØRT) {
            return INGEN_DESTINASJON.also {
                log.info(txt(jp, "Allerede journalført"))
                jp.metrikker(ALLEREDE_JOURNALFØRT)
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
                jp.metrikker(RACE)
            }
        }

        return beslutter.beslutt(jp)
    }

    private fun txt(jp : Journalpost, txt : String) = "Journalpost ${jp.id} $txt (tittel='${jp.tittel}', brevkode='${jp.hovedDokumentBrevkode}')"

    private fun ingen(jp : Journalpost, ekstra : String = "") =
        INGEN_DESTINASJON.also {
            log.info("Journalpost ${jp.id} med status '${jp.status}' fra kanal '${jp.kanal}' skal IKKE fordeles (tittel='${jp.tittel}', brevkode='${jp.hovedDokumentBrevkode}'). $ekstra")
            jp.metrikker(INGEN)
        }

    override fun toString() = "DefaultFordelingBeslutter(cfg=$cfg)"

    companion object {

        private val HÅNDTERES_AV_ANDRE = listOf(EESSI, NAV_NO_CHAT, EKST_OPPS)
    }
}