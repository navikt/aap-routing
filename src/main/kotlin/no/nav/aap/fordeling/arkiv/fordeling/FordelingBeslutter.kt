package no.nav.aap.fordeling.arkiv.fordeling

import org.springframework.stereotype.Component
import no.nav.aap.fordeling.arkiv.fordeling.Fordeler.FordelingResultat.FordelingType.ALLEREDE_JOURNALFØRT
import no.nav.aap.fordeling.arkiv.fordeling.Fordeler.FordelingResultat.FordelingType.INGEN
import no.nav.aap.fordeling.arkiv.fordeling.Fordeler.FordelingResultat.FordelingType.RACE
import no.nav.aap.fordeling.arkiv.fordeling.FordelingBeslutter.BeslutningsStatus.INGEN_FORDELING
import no.nav.aap.fordeling.arkiv.fordeling.FordelingBeslutter.BeslutningsStatus.TIL_FORDELING
import no.nav.aap.fordeling.arkiv.fordeling.FordelingBeslutter.BeslutningsStatus.TIL_MANUELL_FORDELING
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.JournalpostDTO.Kanal.EESSI
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.JournalpostDTO.Kanal.EKST_OPPS
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.JournalpostDTO.Kanal.NAV_NO_CHAT
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.JournalpostDTO.Kanal.UKJENT
import no.nav.aap.fordeling.arkiv.fordeling.Journalpost.JournalpostStatus
import no.nav.aap.fordeling.arkiv.fordeling.Journalpost.JournalpostStatus.JOURNALFØRT
import no.nav.aap.util.LoggerUtil

@Component
class FordelingBeslutter(private val cfg : FordelingConfig = FordelingConfig()) {

    private val log = LoggerUtil.getLogger(FordelingBeslutter::class.java)

    enum class BeslutningsStatus { TIL_FORDELING, INGEN_FORDELING, TIL_MANUELL_FORDELING }

    fun avgjørFordeling(jp : Journalpost, hendelseStatus : String, topic : String) : BeslutningsStatus {
        if (!cfg.isEnabled) {
            return ingen(jp, topic)
        }

        if (jp.erMeldekort()) {
            return ingen(jp, topic, "Meldekort håndteres av andre")
        }

        if (jp.kanal in HÅNDTERES_AV_ANDRE) {
            return ingen(jp, topic, "Kanal ${jp.kanal} håndteres av andre")
        }

        if (jp.status == JOURNALFØRT) {
            log.info("Journalpost ${jp.id}  er allerde journalført  (tittel='${jp.tittel}', brevkode='${jp.hovedDokumentBrevkode}')")
            jp.metrikker(ALLEREDE_JOURNALFØRT, topic)
            return INGEN_FORDELING
        }

        if (jp.bruker == null) {
            log.warn("Ingen bruker er satt på journalposten, sender direkte til manuell journalføring")
            return TIL_MANUELL_FORDELING
        }

        if (jp.status != hendelseStatus.somStatus()) {
            log.warn("Race condition, status endret fra $hendelseStatus til ${jp.status} mellom tidspunkt for mottatt hendelse og hentet journalpost ${jp.id} fra kanal ${jp.kanal} og brevkode ${jp.hovedDokumentBrevkode}, sjekk om noen andre ferdigstiller")
            jp.metrikker(RACE, topic)
            return INGEN_FORDELING
        }

        if (jp.kanal == UKJENT) {
            log.warn("UKjent kanal for journalpost ${jp.id}, oppdater enum og vurder håndtering, fordeler likevel")
        }
        return TIL_FORDELING
    }

    private fun ingen(jp : Journalpost, topic : String, ekstra : String = "") : BeslutningsStatus {
        log.info("Journalpost ${jp.id} med status '${jp.status}' skal IKKE fordeles (tittel='${jp.tittel}', brevkode='${jp.hovedDokumentBrevkode}'). $ekstra")
        jp.metrikker(INGEN, topic)
        return INGEN_FORDELING
    }

    private fun String.somStatus() =
        JournalpostStatus.values().find { it.name.equals(this, ignoreCase = true) } ?: JournalpostStatus.UKJENT

    override fun toString() = "DefaultFordelingBeslutter(cfg=$cfg)"

    companion object {

        private val HÅNDTERES_AV_ANDRE = listOf(EESSI, NAV_NO_CHAT, EKST_OPPS)
    }
}