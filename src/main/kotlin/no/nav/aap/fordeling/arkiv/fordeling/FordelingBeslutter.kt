package no.nav.aap.fordeling.arkiv.fordeling

import org.springframework.stereotype.Component
import no.nav.aap.fordeling.arkiv.ArkivClient
import no.nav.aap.fordeling.arkiv.fordeling.Fordeler.FordelingResultat.FordelingType.ALLEREDE_JOURNALFØRT
import no.nav.aap.fordeling.arkiv.fordeling.Fordeler.FordelingResultat.FordelingType.INGEN
import no.nav.aap.fordeling.arkiv.fordeling.Fordeler.FordelingResultat.FordelingType.RACE
import no.nav.aap.fordeling.arkiv.fordeling.FordelingBeslutter.BeslutningsStatus.INGEN_FORDELING
import no.nav.aap.fordeling.arkiv.fordeling.FordelingBeslutter.BeslutningsStatus.TIL_ARENA_FORDELING
import no.nav.aap.fordeling.arkiv.fordeling.FordelingBeslutter.BeslutningsStatus.TIL_MANUELL_ARENA_FORDELING
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.JournalpostDTO.Kanal.EESSI
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.JournalpostDTO.Kanal.EKST_OPPS
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.JournalpostDTO.Kanal.NAV_NO_CHAT
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.JournalpostDTO.Kanal.UKJENT
import no.nav.aap.fordeling.arkiv.fordeling.Journalpost.JournalpostStatus
import no.nav.aap.fordeling.arkiv.fordeling.Journalpost.JournalpostStatus.JOURNALFØRT
import no.nav.aap.util.LoggerUtil

@Component
class FordelingBeslutter(private val arkiv : ArkivClient, private val cfg : FordelingConfig = FordelingConfig()) {

    private val log = LoggerUtil.getLogger(FordelingBeslutter::class.java)

    enum class BeslutningsStatus { TIL_KELVIN_FORDELING, TIL_ARENA_FORDELING, INGEN_FORDELING, TIL_MANUELL_ARENA_FORDELING }

    fun avgjørFordeling(jp : Journalpost, hendelseStatus : String, topic : String) : BeslutningsStatus {

        /* kotlin.runCatching {
             if (jp.dokumenter.harOriginal()) {
                 arkiv.hentSøknad(jp).also {
                     log.info("Søknad er OK")
                 }
             }

         }.getOrElse { log.warn("OOPS", it) }
         */

        if (!cfg.isEnabled) {
            return ingen(jp, topic, "Fordeling er ikke aktivert")
        }

        if (jp.erMeldekort()) {
            return ingen(jp, topic, "Meldekort håndteres av andre")
        }

        if (jp.status == JOURNALFØRT) {
            log.info(stdText(jp, "Allerede journalført"))
            jp.metrikker(ALLEREDE_JOURNALFØRT, topic)
            return INGEN_FORDELING
        }

        if (jp.bruker == null) {
            log.warn("Ingen bruker er satt på journalposten, sender direkte til manuell journalføring")
            return TIL_MANUELL_ARENA_FORDELING
        }

        if (jp.status != hendelseStatus.somStatus()) {
            log.warn(stdText(jp, "race condition, status endret fra $hendelseStatus til ${jp.status}, sjekk om noen andre ferdigstiller"))
            jp.metrikker(RACE, topic)
            return INGEN_FORDELING
        }

        if (jp.kanal == UKJENT) {
            log.warn(stdText(jp, "har ukjent kanal, fordeles likevel"))
        }

        return TIL_ARENA_FORDELING
    }

    private fun stdText(jp : Journalpost, txt : String) = "Journalpost ${jp.id} $txt (tittel='${jp.tittel}', brevkode='${jp.hovedDokumentBrevkode}')"

    private fun ingen(jp : Journalpost, topic : String, ekstra : String = "") : BeslutningsStatus {
        log.info("Journalpost ${jp.id} med status '${jp.status}' fra kanal '${jp.kanal}' skal IKKE fordeles (tittel='${jp.tittel}', brevkode='${jp.hovedDokumentBrevkode}'). $ekstra")
        jp.metrikker(INGEN, topic)
        return INGEN_FORDELING
    }

    private fun String.somStatus() =
        JournalpostStatus.values().find { it.name.equals(this, ignoreCase = true) } ?: JournalpostStatus.UKJENT

    override fun toString() = "DefaultFordelingBeslutter(cfg=$cfg)"

    companion object {

        val HÅNDTERES_AV_ANDRE = listOf(EESSI, NAV_NO_CHAT, EKST_OPPS)
    }
}