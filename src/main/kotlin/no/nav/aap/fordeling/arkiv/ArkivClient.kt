package no.nav.aap.routing.arkiv

import org.apache.coyote.http11.Constants.a
import org.springframework.stereotype.Component

@Component
class ArkivClient(private val a: ArkivWebClientAdapter) {
    fun journalpost(id: Long)  = a.journalpost(id)
}