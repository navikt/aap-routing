package no.nav.aap.fordeling.arkiv.fordeling

import org.springframework.stereotype.Component
import no.nav.aap.fordeling.arkiv.fordeling.FordelingBeslutter.FordelingsBeslutning
import no.nav.aap.fordeling.arkiv.fordeling.FordelingBeslutter.FordelingsBeslutning.TIL_ARENA

interface InspisererendeBeslutter {

    fun beslutt(jp : Journalpost) : FordelingsBeslutning = TIL_ARENA
}

@Component
class VoidBeslutter : InspisererendeBeslutter

/*
class DefaltInspiserendeBeslutter(val arkiv : ArkivClient, private val mapper : ObjectMapper) : InspisererendeBeslutter {


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