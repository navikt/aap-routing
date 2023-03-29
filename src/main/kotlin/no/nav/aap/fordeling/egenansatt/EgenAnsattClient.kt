package no.nav.aap.fordeling.egenansatt

import no.nav.aap.api.felles.Fødselsnummer
import org.springframework.stereotype.Component

@Component
class EgenAnsattClient(private val a : EgenAnsattWebClientAdapter) {
    fun erEgenAnsatt(fnr : Fødselsnummer) = a.erEgenAnsatt(fnr.fnr)
    override fun toString() = "EgenAnsattClient(a=$a)"
}