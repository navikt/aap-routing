package no.nav.aap.fordeling.arkiv

import org.springframework.stereotype.Component

@Component
class ArkivClient(private val a: ArkivWebClientAdapter) {
    fun journalpost(id: Long)  = a.journalpost(id)
}