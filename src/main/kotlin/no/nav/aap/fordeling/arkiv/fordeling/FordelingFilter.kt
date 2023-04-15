package no.nav.aap.fordeling.arkiv.fordeling

import org.springframework.stereotype.Component
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.JournalpostDTO.Kanal
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.JournalpostDTO.Kanal.EESSI
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.JournalpostDTO.Kanal.EKST_OPPS
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.JournalpostDTO.Kanal.NAV_NO_CHAT
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.JournalpostDTO.Kanal.UKJENT
import no.nav.aap.fordeling.arkiv.fordeling.Journalpost.JournalpostStatus
import no.nav.aap.fordeling.arkiv.fordeling.Journalpost.JournalpostStatus.MOTTATT
import no.nav.aap.util.Constants.AAP
import no.nav.joarkjournalfoeringhendelser.JournalfoeringHendelseRecord

@Component
class FordelingFilter(private val ignorerteKanaler : List<Kanal> = HÅNDTERES_AV_ANDRE) {

    fun kanFordele(hendelse : JournalfoeringHendelseRecord) =
        with(hendelse) {
            !fraIgnorertKanal() && erAAP() && erMottatt()
        }

    private fun JournalfoeringHendelseRecord.fraIgnorertKanal() = kanal() in ignorerteKanaler

    companion object {

        private fun JournalfoeringHendelseRecord.erMottatt() = status() == MOTTATT

        private fun JournalfoeringHendelseRecord.kanal() = Kanal.values().find { it.name == mottaksKanal } ?: UKJENT

        private fun JournalfoeringHendelseRecord.erAAP() = temaNytt.lowercase() == AAP

        fun JournalfoeringHendelseRecord.status() =
            JournalpostStatus.values().find { it.name.equals(this.journalpostStatus, ignoreCase = true) } ?: JournalpostStatus.UKJENT

        private val HÅNDTERES_AV_ANDRE = listOf(EESSI, NAV_NO_CHAT, EKST_OPPS)
    }
}