package no.nav.aap.fordeling.arkiv.fordeling

import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.JournalpostDTO.JournalStatus.MOTTATT
import no.nav.boot.conditionals.ConditionalOnGCP

@ConditionalOnGCP
class MottattAwareFordelingBeslutter: FordelingBeslutter {

    override fun skalFordele(jp: Journalpost) = jp.status == MOTTATT

}

interface FordelingBeslutter {
    fun skalFordele(jp:Journalpost) : Boolean

}