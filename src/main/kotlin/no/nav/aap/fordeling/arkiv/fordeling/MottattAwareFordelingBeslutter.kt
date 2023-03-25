package no.nav.aap.fordeling.arkiv.fordeling

import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.JournalpostDTO.JournalStatus.MOTTATT
import no.nav.boot.conditionals.ConditionalOnGCP

@ConditionalOnGCP
class MottattAwareFordelingBeslutter(private val cfg: FordelingConfig): FordelingBeslutter {
    override fun skalFordele(jp: Journalpost) = cfg.isEnabled //&& jp.status == MOTTATT
}

interface FordelingBeslutter {
    fun skalFordele(jp:Journalpost) : Boolean

}