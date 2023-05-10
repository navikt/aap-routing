package no.nav.aap.fordeling.fordeling

import no.nav.aap.fordeling.arkiv.journalpost.Journalpost
import org.springframework.stereotype.Component
import no.nav.aap.fordeling.fordeling.FordelingAvOppgaveUtvelger.FordelingsBeslutning
import no.nav.aap.fordeling.fordeling.FordelingAvOppgaveUtvelger.FordelingsBeslutning.ARENA

interface Beslutter {

    fun beslutt(jp : Journalpost) : FordelingsBeslutning
}

@Component
class ArenaBeslutter : Beslutter {

    override fun beslutt(jp : Journalpost) = ARENA
}