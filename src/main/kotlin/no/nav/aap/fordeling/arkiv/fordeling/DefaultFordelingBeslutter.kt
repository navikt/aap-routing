package no.nav.aap.fordeling.arkiv.fordeling

import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.JournalpostDTO.JournalStatus.MOTTATT
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.JournalpostDTO.Kanal.EESSI
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.JournalpostDTO.Kanal.NAV_NO_CHAT
import org.springframework.stereotype.Component

@Component
class DefaultFordelingBeslutter(private val cfg : FordelingConfig) : FordelingBeslutter {
    override fun skalFordele(jp : Journalpost) =
        with(jp) {
            cfg.isEnabled
                && status == MOTTATT
                && !erMeldekort()
                && kanal !in HÅNDTERES_AV_ANDRE
        }

    override fun toString() = "DefaultFordelingBeslutter(cfg=$cfg)"

    companion object {
        private val HÅNDTERES_AV_ANDRE = listOf(EESSI, NAV_NO_CHAT)
    }
}

interface FordelingBeslutter {

    fun skalFordele(jp : Journalpost) : Boolean
}