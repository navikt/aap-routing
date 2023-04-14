package no.nav.aap.fordeling.egenansatt

import io.micrometer.observation.annotation.Observed
import org.springframework.stereotype.Component
import no.nav.aap.api.felles.Fødselsnummer

@Component
@Observed(name = "EgenAnsatt")
class EgenAnsattClient(private val a : EgenAnsattWebClientAdapter) {

    fun erEgenAnsatt(fnr : Fødselsnummer) = a.erEgenAnsatt(fnr.fnr)

    override fun toString() = "EgenAnsattClient(a=$a)"
}