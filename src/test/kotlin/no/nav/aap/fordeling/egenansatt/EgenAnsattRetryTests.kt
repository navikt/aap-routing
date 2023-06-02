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
import no.nav.aap.fordeling.fordeling.Fordeler.Companion.FIKTIVTFNR
import no.nav.aap.fordeling.utils.MockWebServerExtensions.expect
import no.nav.aap.util.AccessorUtil
import no.nav.aap.util.LoggerUtil
import no.nav.aap.util.MDCUtil

class EgenAnsattRetryTests {

    private val log = LoggerUtil.getLogger(EgenAnsattRetryTests::class.java)

    lateinit var egenServer : MockWebServer
    lateinit var client : EgenAnsattClient

    init {
        AccessorUtil.init()
        MDCUtil.callId()
    }

    @BeforeEach
    fun beforeEach() {
        egenServer = MockWebServer()
        with(EgenAnsattConfig(egenServer.url("/").toUri())) {
            client = EgenAnsattClient(EgenAnsattWebClientAdapter(WebClient.builder().baseUrl("$baseUri").build(), this))
        }
    }

    @Test
    fun ingenRetry() {
        log.info("Main thread")
        egenServer.expect("false")
        assertThat(client.erEgenAnsatt(FIKTIVTFNR)).isFalse
        assertThat(egenServer.requestCount).isEqualTo(1)
    }

    @Test
    @DisplayName("Retry fikser det tilslutt")
    fun retryFunker() {
        egenServer.expect("false", BAD_GATEWAY, BAD_GATEWAY, OK)
        assertThat(client.erEgenAnsatt(FIKTIVTFNR)).isFalse
        assertThat(egenServer.requestCount).isEqualTo(3)
    }

    @Test
    @DisplayName("Retry gir opp tilslutt")
    fun retryGirOpp() {
        egenServer.expect(4, BAD_GATEWAY)
        assertThrows<IntegrationException> { client.erEgenAnsatt(FIKTIVTFNR) }
        assertThat(egenServer.requestCount).isEqualTo(4)
    }

    @Test
    @DisplayName("Error 400  ingen retry")
    fun ingenRetry400() {
        egenServer.expect("false", BAD_REQUEST)
        assertThrows<IrrecoverableIntegrationException> { client.erEgenAnsatt(FIKTIVTFNR) }
        assertThat(egenServer.requestCount).isEqualTo(1)
    }
}