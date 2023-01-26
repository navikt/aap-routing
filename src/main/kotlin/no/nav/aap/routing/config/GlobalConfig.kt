package no.nav.aap.routing.config

import java.time.Duration
import java.util.function.Consumer
import no.nav.aap.util.LoggerUtil.getLogger
import no.nav.security.token.support.client.core.OAuth2ClientException
import no.nav.security.token.support.client.core.http.OAuth2HttpClient
import no.nav.security.token.support.client.core.http.OAuth2HttpRequest
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenResponse
import no.nav.security.token.support.client.spring.oauth2.ClientConfigurationPropertiesMatcher
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.reactive.function.client.WebClient
import reactor.util.retry.Retry.fixedDelay

@Configuration
class GlobalConfig {

    @Bean
    fun configMatcher() =
        object : ClientConfigurationPropertiesMatcher {}

    @Bean
    fun retryingOAuth2HttpClient(b: WebClient.Builder) = RetryingWebClientOAuth2HttpClient(b.build())

    class RetryingWebClientOAuth2HttpClient(private val client: WebClient) : OAuth2HttpClient {

        private val log = getLogger(javaClass)

        override fun post(req: OAuth2HttpRequest) =
            with(req) {
                client.post()
                    .uri(tokenEndpointUrl)
                    .headers { Consumer<HttpHeaders> { it.putAll(oAuth2HttpHeaders.headers()) } }
                    .bodyValue(LinkedMultiValueMap<String, String>().apply { setAll(formParameters) })
                    .retrieve()
                    .bodyToMono(OAuth2AccessTokenResponse::class.java)
                    .retryWhen(retry())
                    .onErrorMap { e -> if (e is OAuth2ClientException) e else OAuth2ClientException("Uventet feil fra token endpoint",e) }
                    .doOnSuccess { log.trace("Token endpoint returnerte OK") }
                    .block()
                    ?: throw OAuth2ClientException("Ingen respons (null) fra token endpoint $tokenEndpointUrl")
            }
        private fun retry() =
            fixedDelay(3, Duration.ofMillis(100))
                .filter { e -> e is OAuth2ClientException }
                .doBeforeRetry { s -> log.info("Retry kall mot token endpoint feilet med  ${s.failure().message} for ${s.totalRetriesInARow() + 1} gang, prøver igjen",s.failure()) }
                .onRetryExhaustedThrow { _, spec ->  spec.failure().also { log.warn("Retry mot token endpoint gir opp etter ${spec.totalRetriesInARow()} forsøk") } }

    }
}