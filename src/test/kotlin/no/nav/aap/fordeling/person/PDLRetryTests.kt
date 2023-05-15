package no.nav.aap.fordeling.person

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.graphql.client.HttpGraphQlClient
import org.springframework.http.HttpHeaders.CONTENT_TYPE
import org.springframework.http.HttpStatus.OK
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.web.reactive.function.client.WebClient
import no.nav.aap.api.felles.error.IrrecoverableGraphQLException.NotFoundGraphQLException
import no.nav.aap.api.felles.error.RecoverableGraphQLException.UnhandledGraphQLException
import no.nav.aap.api.felles.graphql.LoggingGraphQLInterceptor
import no.nav.aap.fordeling.fordeling.Fordeler.Companion.FIKTIVTFNR
import no.nav.aap.fordeling.person.Diskresjonskode.ANY
import no.nav.aap.fordeling.person.MockWebServerExtensions.expect
import no.nav.aap.rest.AbstractWebClientAdapter.Companion.correlatingFilterFunction
import no.nav.aap.util.AccessorUtil
import no.nav.aap.util.LoggerUtil
import no.nav.aap.util.MDCUtil

class PDLRetryTests {

    private val log = LoggerUtil.getLogger(PDLRetryTests::class.java)

    lateinit var pdl : MockWebServer
    lateinit var pdlClient : PDLClient

    init {
        MDCUtil.callId()
        AccessorUtil.init()
    }

    @BeforeEach
    fun beforeEach() {
        log.info("Main thread")
        pdl = MockWebServer()
        with(PDLConfig(pdl.url("/graphql").toUri())) {
            val webClient = WebClient.builder()
                .baseUrl("$baseUri")
                .filter(correlatingFilterFunction("test"))
                .build()
            pdlClient = PDLClient(PDLWebClientAdapter(webClient, HttpGraphQlClient
                .builder(webClient)
                .interceptor(LoggingGraphQLInterceptor())
                .build(), this))
        }
    }

    @Test
    fun pdlRetryOK() {
        pdl.expect(ERROR, OK)
        assertThat(pdlClient.diskresjonskode(FIKTIVTFNR)).isEqualTo(ANY)
        assertThat(pdl.requestCount).isEqualTo(2)
    }

    @Test
    fun pdlRetryFail() {
        pdl.expect(4, ERROR)
        assertThrows<UnhandledGraphQLException> { pdlClient.diskresjonskode(FIKTIVTFNR) }
        assertThat(pdl.requestCount).isEqualTo(4)
    }

    @Test
    fun pdlNotFoundIngenRetry() {
        pdl.expect(NOT_FOUND)
        assertThrows<NotFoundGraphQLException> { pdlClient.diskresjonskode(FIKTIVTFNR) }
        assertThat(pdl.requestCount).isEqualTo(1)
    }

    companion object {

        const val ERROR = """
            {
              "errors": [
                {
                  "message": "Rare greier fra pdl",
                  "locations": [],
                  "path": [],
                  "extensions": {
                    "code": "skal bli retry",
                    "details": null,
                    "classification": "ExecutionAborted"
                  }
                }
              ]
            }
        """

        const val NOT_FOUND = """
            {
              "errors": [
                {
                  "message": "Fant ikke person pdl",
                  "locations": [],
                  "path": [],
                  "extensions": {
                    "code": "not_found",
                    "details": null,
                    "classification": "ExecutionAborted"
                  }
                }
              ]
            }
        """

        const val OK = """ {
          "data": {
           "adressebeskyttelse": {
              "gradering": []
           }
          }
       }
        """
    }
}

object MockWebServerExtensions {

    fun MockWebServer.expect(times : Int, body : String) {
        for (i in 1..times) expect(body)
    }

    fun MockWebServer.expect(vararg bodies : String) =
        bodies.iterator().forEach {
            enqueue(MockResponse().setResponseCode(OK.value()).apply {
                setHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                setBody(it)
            })
        }
}