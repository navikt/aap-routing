package no.nav.aap.fordeling.arkiv.fordeling

import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.JournalpostDTO.JournalStatus.MOTTATT
import org.springframework.stereotype.Component

@Component
class DefaultFordelingBeslutter(private val cfg: FordelingConfig): FordelingBeslutter {
    override fun skalFordele(jp: Journalpost) = cfg.isEnabled && jp.status == MOTTATT  && !jp.erMeldekort()
}

interface FordelingBeslutter {
    fun skalFordele(jp:Journalpost) : Boolean

}