package no.nav.aap.fordeling.arkiv.fordeling

import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.JournalpostDTO.JournalStatus.MOTTATT
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.JournalpostDTO.Kanal.*
import org.springframework.stereotype.Component

@Component
class DefaultFordelingBeslutter(private val cfg: FordelingConfig): FordelingBeslutter {
    override fun skalFordele(jp: Journalpost) = cfg.isEnabled
            && jp.status == MOTTATT
            && !jp.erMeldekort()
            && jp.kanal !in IGNORERTE_KANALER

    companion object {
        private val IGNORERTE_KANALER = listOf(EESSI, NAV_NO_CHAT)
    }
}

interface FordelingBeslutter {

    fun skalFordele(jp:Journalpost) : Boolean

}