package no.nav.aap.fordeling.arkiv

import no.nav.security.token.support.spring.UnprotectedRestController
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam

@UnprotectedRestController(value = ["/dev"])
class DevController(private val arkiv: ArkivWebClientAdapter) {
    @PostMapping("ferdigstill")
    fun ferdigstill(@RequestBody  jp: Journalpost, @RequestParam enhetNr: String,@RequestParam saksNr: String) = arkiv.oppdater(jp.oppdateringsData(saksNr,enhetNr))
}