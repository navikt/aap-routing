package no.nav.aap.fordeling.arena
import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.fordeling.arena.ArenaConfig.Companion.PERSON
import no.nav.aap.util.Constants.AAP

class ArenaDTOs {
    data class ArenaSakForespørsel(val fnr: Fødselsnummer,val brukertype: String = PERSON,val tema: String = AAP,  val lukket: Boolean = false)
}