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
    fun geoTilknytning(fnr : Fødselsnummer) = //runCatching {
        a.geoTilknytning(fnr).also {
            log.info("spring graphql geo ok")
        }

    fun diskresjonskode(fnr : Fødselsnummer) = // runCatching {
        a.diskresjonskode(fnr).also {
            log.info("spring graphql diskresjon ok")
        }

    fun fnr(id : AktørId) = //runCatching {
        a.fnr(id).also {
            log.info("spring graphql fnr ok")
        }
}