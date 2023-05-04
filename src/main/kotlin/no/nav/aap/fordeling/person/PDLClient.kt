package no.nav.aap.fordeling.person

import io.micrometer.observation.annotation.Observed
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import no.nav.aap.api.felles.AktørId
import no.nav.aap.api.felles.Fødselsnummer

@Component
@Observed(contextualName = "PDL")
class PDLClient(private val a : PDLWebClientAdapter) {

    private val log = LoggerFactory.getLogger(PDLClient::class.java)
    fun geoTilknytning(fnr : Fødselsnummer) = runCatching {
        a.geoTilknytning1(fnr).also {
            log.info("spring graphql ok")
        }
    }.getOrElse {
        log.warn("spring graphql geo feil", it)
        a.geoTilknytning(fnr)
    }

    fun diskresjonskode(fnr : Fødselsnummer) = runCatching {
        a.diskresjonskode1(fnr).also {
            log.info("spring graphql ok")
        }
    }.getOrElse {
        log.warn("spring graphql diskresjon feil", it)
        a.diskresjonskode(fnr)
    }

    fun fnr(id : AktørId) = runCatching {
        a.fnr1(id).also {
            log.info("spring graphql ok")
        }
    }.getOrElse {
        log.warn("spring graphql fnr feil", it)
        a.fnr(id)
    }
}