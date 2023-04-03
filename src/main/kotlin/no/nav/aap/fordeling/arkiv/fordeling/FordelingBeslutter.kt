package no.nav.aap.fordeling.arkiv.fordeling

import org.springframework.stereotype.Component
import no.nav.aap.fordeling.arkiv.fordeling.Fordeler.FordelingResultat.FordelingType.ALLEREDE_JOURNALFØRT
import no.nav.aap.fordeling.arkiv.fordeling.Fordeler.FordelingResultat.FordelingType.INGEN
import no.nav.aap.fordeling.arkiv.fordeling.Fordeler.FordelingResultat.FordelingType.RACE
import no.nav.aap.fordeling.arkiv.fordeling.FordelingBeslutter.FordelingsBeslutning.INGEN_FORDELING
import no.nav.aap.fordeling.arkiv.fordeling.FordelingBeslutter.FordelingsBeslutning.TIL_GOSYS
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.JournalpostDTO.Kanal.EESSI
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.JournalpostDTO.Kanal.EKST_OPPS
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.JournalpostDTO.Kanal.NAV_NO_CHAT
import no.nav.aap.fordeling.arkiv.fordeling.Journalpost.JournalpostStatus
import no.nav.aap.fordeling.arkiv.fordeling.Journalpost.JournalpostStatus.JOURNALFØRT
import no.nav.aap.fordeling.arkiv.fordeling.Journalpost.JournalpostStatus.values
import no.nav.aap.util.LoggerUtil

@Component
class FordelingBeslutter(private val inspektør : InspisererendeBeslutter, private val cfg : FordelingConfig = FordelingConfig()) {

    private val log = LoggerUtil.getLogger(FordelingBeslutter::class.java)

    enum class FordelingsBeslutning { TIL_KELVIN, TIL_ARENA, INGEN_FORDELING, TIL_GOSYS }

    fun avgjørFordeling(jp : Journalpost, hendelseStatus : String, topic : String) : FordelingsBeslutning {

        if (!cfg.isEnabled) {
            return ingen(jp, topic, "Fordeling er ikke aktivert")
        }

        if (jp.erMeldekort()) {
            return ingen(jp, topic, "Meldekort håndteres av andre")
        }

        if (jp.status == JOURNALFØRT) {
            log.info(txt(jp, "Allerede journalført"))
            jp.metrikker(ALLEREDE_JOURNALFØRT, topic)
            return INGEN_FORDELING
        }

        if (jp.bruker == null) {
            log.warn("Ingen bruker er satt på journalposten, sender direkte til manuell journalføring")
            return TIL_GOSYS
        }

        if (jp.status != hendelseStatus.somStatus()) {
            log.warn(txt(jp, "race condition, status endret fra $hendelseStatus til ${jp.status}, sjekk om noen andre ferdigstiller"))
            jp.metrikker(RACE, topic)
            return INGEN_FORDELING
        }

        // TODO Inspiser søknaden, avgjør om dem skal til Arena eller ikke
        return inspektør.beslutt(jp)
    }

    private fun txt(jp : Journalpost, txt : String) = "Journalpost ${jp.id} $txt (tittel='${jp.tittel}', brevkode='${jp.hovedDokumentBrevkode}')"

    private fun ingen(jp : Journalpost, topic : String, ekstra : String = "") : FordelingsBeslutning {
        log.info("Journalpost ${jp.id} med status '${jp.status}' fra kanal '${jp.kanal}' skal IKKE fordeles (tittel='${jp.tittel}', brevkode='${jp.hovedDokumentBrevkode}'). $ekstra")
        jp.metrikker(INGEN, topic)
        return INGEN_FORDELING
    }

    private fun String.somStatus() =
        values().find { it.name.equals(this, ignoreCase = true) } ?: JournalpostStatus.UKJENT

    override fun toString() = "DefaultFordelingBeslutter(cfg=$cfg)"

    companion object {

        val HÅNDTERES_AV_ANDRE = listOf(EESSI, NAV_NO_CHAT, EKST_OPPS)
    }
}