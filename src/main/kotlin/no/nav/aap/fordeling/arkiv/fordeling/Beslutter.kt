package no.nav.aap.fordeling.arkiv.fordeling

import org.springframework.stereotype.Component
import no.nav.aap.fordeling.arkiv.fordeling.DestinasjonUtvelger.Destinasjon
import no.nav.aap.fordeling.arkiv.fordeling.DestinasjonUtvelger.Destinasjon.ARENA

interface Beslutter {

    fun beslutt(jp : Journalpost) : Destinasjon
}

@Component
class ArenaBeslutter : Beslutter {

    override fun beslutt(jp : Journalpost) = ARENA
}

/*
class HoveddokumentInspiserendeBeslutter(val arkiv : ArkivClient, private val mapper : ObjectMapper) : InspisererendeBeslutter {


    private val log = LoggerUtil.getLogger(DefaltInspiserendeBeslutter::class.java)

    override fun beslutt(jp : Journalpost) : FordelingsBeslutning {
        return runCatching {
            if (jp.harOriginal()) {
                arkiv.hentSøknad(jp).also {
                    it?.let {
                        if (gyldigJson(it)) {
                            log.info("Søknad er gyldig JSON")
                        }
                    }
                }
                TIL_ARENA
            }
        }.getOrElse {
            TIL_ARENA as FordelingsBeslutning
        }
    }

    private fun gyldigJson(json : String) =
        runCatching {
            mapper.readTree(json)
            true
        }.getOrElse {
            log.warn("Ugyldig json", it)
            false
        }
}
 */