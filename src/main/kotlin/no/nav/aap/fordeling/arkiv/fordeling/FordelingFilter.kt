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

    fun kanFordele(h : JournalfoeringHendelseRecord) = h.kanal() !in ignorerteKanaler && h.temaNytt.lowercase() == AAP && h.status() == MOTTATT

    private fun JournalfoeringHendelseRecord.kanal() = Kanal.values().find { it.name == mottaksKanal } ?: UKJENT

    companion object {

        fun JournalfoeringHendelseRecord.status() =
            JournalpostStatus.values().find { it.name.equals(this.journalpostStatus, ignoreCase = true) } ?: JournalpostStatus.UKJENT

        private val HÅNDTERES_AV_ANDRE = listOf(EESSI, NAV_NO_CHAT, EKST_OPPS)
    }
}