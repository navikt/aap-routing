package no.nav.aap.fordeling.arkiv.fordeling

import org.springframework.stereotype.Component
import no.nav.aap.fordeling.arkiv.fordeling.JournalpostDestinasjonUtvelger.FordelingsBeslutning
import no.nav.aap.fordeling.arkiv.fordeling.JournalpostDestinasjonUtvelger.FordelingsBeslutning.ARENA

interface Beslutter {

    fun beslutt(jp : Journalpost) : FordelingsBeslutning
}

@Component
class ArenaBeslutter : Beslutter {

    override fun beslutt(jp : Journalpost) = ARENA
}