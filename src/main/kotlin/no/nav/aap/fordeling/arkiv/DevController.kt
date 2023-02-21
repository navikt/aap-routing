package no.nav.aap.fordeling.arkiv

import no.nav.aap.fordeling.arkiv.JournalpostDTO.OppdateringData
import no.nav.security.token.support.spring.UnprotectedRestController
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam

@UnprotectedRestController(value = ["/dev"])
class DevController(private val arkiv: ArkivWebClientAdapter) {
    @PostMapping("ferdigstill")
    fun ferdigstill(@RequestBody  data:OppdateringData) = arkiv.oppdater(data)
}