package no.nav.aap.fordeling.arkiv.fordeling

import org.springframework.stereotype.Component
import no.nav.aap.fordeling.arkiv.fordeling.DestinasjonUtvelger.Destinasjon.GOSYS
import no.nav.aap.fordeling.arkiv.fordeling.DestinasjonUtvelger.Destinasjon.INGEN_DESTINASJON
import no.nav.aap.fordeling.arkiv.fordeling.Fordeler.FordelingResultat.FordelingType
import no.nav.aap.fordeling.arkiv.fordeling.Fordeler.FordelingResultat.FordelingType.ALLEREDE_JOURNALFØRT
import no.nav.aap.fordeling.arkiv.fordeling.Fordeler.FordelingResultat.FordelingType.INGEN
import no.nav.aap.fordeling.arkiv.fordeling.Fordeler.FordelingResultat.FordelingType.RACE
import no.nav.aap.fordeling.arkiv.fordeling.Journalpost.JournalpostStatus
import no.nav.aap.fordeling.arkiv.fordeling.Journalpost.JournalpostStatus.JOURNALFØRT
import no.nav.aap.util.LoggerUtil

@Component
class DestinasjonUtvelger(private val beslutter : Beslutter, private val cfg : FordelingConfig = FordelingConfig()) {

    private val log = LoggerUtil.getLogger(DestinasjonUtvelger::class.java)

    enum class Destinasjon { KELVIN, ARENA, INGEN_DESTINASJON, GOSYS }

    fun destinasjon(jp : Journalpost, hendelseStatus : JournalpostStatus) : Destinasjon {

        if (!cfg.isEnabled) {
            return ingenDestinasjon(jp, "Fordeling er ikke aktivert", INGEN)
        }

        if (jp.erMeldekort()) {
            return ingenDestinasjon(jp, "Meldekort håndteres av andre", INGEN)
        }

        if (jp.status == JOURNALFØRT) {
            return ingenDestinasjon(jp, "Allerede journalført", ALLEREDE_JOURNALFØRT)
        }

        if (jp.bruker == null) {
            return GOSYS.also {
                log.warn("Ingen bruker er satt på journalposten, sender direkte til manuell journalføring")
            }
        }

        if (jp.status != hendelseStatus) {
            return ingenDestinasjon(jp, "race condition, status endret fra $hendelseStatus til ${jp.status}, sjekk om noen andre ferdigstiller", RACE)
        }

        return beslutter.beslutt(jp)
    }

    private fun ingenDestinasjon(jp : Journalpost, ekstra : String = "", type : FordelingType) =
        INGEN_DESTINASJON.also {
            log.info("Journalpost ${jp.id} med status '${jp.status}' fra kanal '${jp.kanal}' skal IKKE fordeles (tittel='${jp.tittel}', brevkode='${jp.hovedDokumentBrevkode}'). $ekstra")
            jp.metrikker(type)
        }

    override fun toString() = "DefaultFordelingBeslutter(cfg=$cfg)"
}