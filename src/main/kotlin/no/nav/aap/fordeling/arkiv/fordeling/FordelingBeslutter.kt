package no.nav.aap.fordeling.arkiv.fordeling

import org.springframework.stereotype.Component
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.JournalpostDTO.Kanal.EESSI
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.JournalpostDTO.Kanal.EKST_OPPS
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.JournalpostDTO.Kanal.NAV_NO_CHAT

@Component
class FordelingBeslutter(private val cfg : FordelingConfig = FordelingConfig()) {

    fun skalFordele(jp : Journalpost) =
        with(jp) {
            cfg.isEnabled
                && !erMeldekort()
                && kanal !in HÅNDTERES_AV_ANDRE
        }

    override fun toString() = "DefaultFordelingBeslutter(cfg=$cfg)"

    companion object {

        private val HÅNDTERES_AV_ANDRE = listOf(EESSI, NAV_NO_CHAT, EKST_OPPS)
    }
}