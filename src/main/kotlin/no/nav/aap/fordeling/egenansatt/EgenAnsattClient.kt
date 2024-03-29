package no.nav.aap.fordeling.egenansatt

import org.springframework.stereotype.Component
import no.nav.aap.api.felles.Fødselsnummer

@Component
class EgenAnsattClient(private val a : EgenAnsattWebClientAdapter) {

    fun erEgenAnsatt(fnr : Fødselsnummer) = a.erEgenAnsatt(fnr.fnr)

    override fun toString() = "EgenAnsattClient(a=$a)"
}