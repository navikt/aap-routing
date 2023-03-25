package no.nav.aap.fordeling.arkiv.fordeling

import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.JournalpostDTO.JournalStatus.MOTTATT
import no.nav.boot.conditionals.ConditionalOnGCP
import no.nav.joarkjournalfoeringhendelser.JournalfoeringHendelseRecord

@ConditionalOnGCP
class MottattAwareFordelingBeslutter(private val cfg: FordelingConfig): FordelingBeslutter {
    override fun skalFordele(jp: JournalfoeringHendelseRecord) = cfg.isEnabled && jp.journalpostStatus == MOTTATT.name
}

interface FordelingBeslutter {
    fun skalFordele(jp:JournalfoeringHendelseRecord) : Boolean

}