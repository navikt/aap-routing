package no.nav.aap.fordeling.egenansatt

import okhttp3.mockwebserver.MockWebServer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.http.HttpStatus.*
import org.springframework.web.reactive.function.client.WebClient
import no.nav.aap.api.felles.error.IntegrationException
import no.nav.aap.api.felles.error.IrrecoverableIntegrationException
import no.nav.aap.fordeling.arkiv.fordeling.Fordeler.Companion.FIKTIVTFNR
import no.nav.aap.fordeling.utils.MockWebServerExtensions.expect

class EgenAnsattRetryTests {

    lateinit var egenServer : MockWebServer
    lateinit var client : EgenAnsattClient

    @BeforeEach
    fun beforeEach() {
        egenServer = MockWebServer()
        with(EgenAnsattConfig(egenServer.url("/").toUri())) {
            client = EgenAnsattClient(EgenAnsattWebClientAdapter(WebClient.builder().baseUrl("$baseUri").build(), this))
        }
    }

    @Test
    fun ingenRetry() {
        egenServer.expect("false")
        assertThat(client.erEgenAnsatt(FIKTIVTFNR)).isFalse
    }

    @Test
    @DisplayName("Retry fikser det tilslutt")
    fun retryFunker() {
        egenServer.expect("false", BAD_GATEWAY, BAD_GATEWAY, OK)
        assertThat(client.erEgenAnsatt(FIKTIVTFNR)).isFalse
    }

    @Test
    @DisplayName("Retry gir opp tilslutt")
    fun retryGirOpp() {
        egenServer.expect(4, BAD_GATEWAY)
        assertThrows<IntegrationException> { client.erEgenAnsatt(FIKTIVTFNR) }
    }

    @Test
    @DisplayName("Error 400  ingen retry")
    fun ingenRetry400() {
        egenServer.expect("false", BAD_REQUEST)
        assertThrows<IrrecoverableIntegrationException> { client.erEgenAnsatt(FIKTIVTFNR) }
    }
}