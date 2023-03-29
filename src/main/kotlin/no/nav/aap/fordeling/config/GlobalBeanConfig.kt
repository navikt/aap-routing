package no.nav.aap.fordeling.config

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.config.MeterFilter.*
import io.netty.channel.ChannelOption.CONNECT_TIMEOUT_MILLIS
import io.netty.handler.logging.LogLevel.*
import io.netty.handler.timeout.WriteTimeoutHandler
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.License
import no.nav.aap.fordeling.util.MetrikkLabels.TITTEL
import no.nav.aap.rest.AbstractWebClientAdapter.Companion.correlatingFilterFunction
import no.nav.aap.util.ChaosMonkey
import no.nav.aap.util.ChaosMonkey.MonkeyExceptionType.RECOVERABLE
import no.nav.aap.util.LoggerUtil.getLogger
import no.nav.aap.util.TokenExtensions.bearerToken
import no.nav.aap.util.WebClientExtensions.toResponse
import no.nav.boot.conditionals.Cluster.*
import no.nav.boot.conditionals.Cluster.Companion.devClusters
import no.nav.boot.conditionals.ConditionalOnNotProd
import no.nav.boot.conditionals.ConditionalOnProd
import no.nav.security.token.support.client.core.OAuth2ClientException
import no.nav.security.token.support.client.core.http.OAuth2HttpClient
import no.nav.security.token.support.client.core.http.OAuth2HttpRequest
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenResponse
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenService
import no.nav.security.token.support.client.spring.ClientConfigurationProperties
import no.nav.security.token.support.client.spring.oauth2.ClientConfigurationPropertiesMatcher
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer
import org.springframework.boot.actuate.endpoint.SanitizingFunction
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer
import org.springframework.boot.info.BuildProperties
import org.springframework.boot.web.reactive.function.client.WebClientCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders.*
import org.springframework.http.HttpStatus.*
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.ExchangeFilterFunction.*
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import reactor.netty.transport.logging.AdvancedByteBufFormat.TEXTUAL
import reactor.util.retry.Retry.fixedDelay
import java.time.Duration
import java.time.Duration.ofSeconds
import java.util.*
import java.util.concurrent.TimeUnit.*

@Configuration
class GlobalBeanConfig(@Value("\${spring.application.name}") private val applicationName : String) {

    private val log = getLogger(GlobalBeanConfig::class.java)

    @Bean
    fun monkey() = ChaosMonkey()

    @Bean
    fun meterRegistryCustomizer() : MeterRegistryCustomizer<MeterRegistry> = MeterRegistryCustomizer { reg ->
        reg.config()
            .meterFilter(replaceTagValues(TITTEL, {
                if (it.contains("Meldekort for uke", ignoreCase = true)) "Meldekort" else it
            })).meterFilter(replaceTagValues(TITTEL, {
                if (it.contains("korrigert meldekort", ignoreCase = true)) "Korrigert meldekort" else it
            }))
    }

    @Bean
    fun swagger(p : BuildProperties) : OpenAPI {
        return OpenAPI()
            .info(Info()
                .title("AAP routing")
                .description("Routing ala KRUT (bare bedre)")
                .version(p.version)
                .license(License()
                    .name("MIT")
                    .url("https://nav.no"))
                 )
    }

    @Bean
    fun webClientCustomizer(client : HttpClient, monkey : ChaosMonkey) =
        WebClientCustomizer { b ->
            b.clientConnector(ReactorClientHttpConnector(client))
                .filter(correlatingFilterFunction(applicationName))
                .filter(monkey.chaosMonkeyRequestFilterFunction(monkey.criteria(devClusters(), 10), RECOVERABLE))
        }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private interface IgnoreUnknown

    @Bean
    fun objectMapperCustomizer() = Jackson2ObjectMapperBuilderCustomizer {
        it.apply {
            mixIn(OAuth2AccessTokenResponse::class.java, IgnoreUnknown::class.java)
            modules(JavaTimeModule(), KotlinModule.Builder().build())
        }
    }

    @ConditionalOnNotProd
    @Bean
    fun notProdHttpClient() = HttpClient.create().wiretap(javaClass.name, TRACE, TEXTUAL)
        .doOnConnected {
            it.addHandlerFirst(WriteTimeoutHandler(90, SECONDS))
        }
        .responseTimeout(ofSeconds(90))
        .option(CONNECT_TIMEOUT_MILLIS, SECONDS.toMillis(10).toInt())

    @ConditionalOnProd
    @Bean
    fun prodHttpClient() = HttpClient.create()//.wiretap(javaClass.name, TRACE, TEXTUAL)

    @Bean
    fun configMatcher() = object : ClientConfigurationPropertiesMatcher {}

    @Bean
    fun sanitizer() = SanitizingFunction {
        with(it) {
            if (key.contains("jwk", ignoreCase = true)) {
                withValue(MASK);
            }
            if (key.contains("private-key", ignoreCase = true)) {
                withValue(MASK);
            }
            if (key.contains("password", ignoreCase = true)) {
                withValue(MASK);
            }
        }
        it
    }

    @Bean
    fun retryingOAuth2HttpClient(b : WebClient.Builder) = RetryingWebClientOAuth2HttpClient(b.build())

    class RetryingWebClientOAuth2HttpClient(private val client : WebClient) : OAuth2HttpClient {

        private val log = getLogger(GlobalBeanConfig::class.java)

        override fun post(req : OAuth2HttpRequest) =
            with(req) {
                client.post()
                    .uri(tokenEndpointUrl)
                    .headers { it.putAll(oAuth2HttpHeaders.headers()) }
                    .bodyValue(LinkedMultiValueMap<String, String>().apply { setAll(formParameters) })
                    .exchangeToMono { it.toResponse<OAuth2AccessTokenResponse>(log) }
                    .retryWhen(retry())
                    .onErrorMap { e ->
                        e as? OAuth2ClientException
                            ?: OAuth2ClientException("Uventet feil fra token endpoint $tokenEndpointUrl", e)
                    }
                    .doOnSuccess { log.trace("Token endpoint $tokenEndpointUrl returnerte OK") }
                    .block()
                    ?: throw OAuth2ClientException("Ingen respons (null) fra token endpoint $tokenEndpointUrl")
            }

        private fun retry() =
            fixedDelay(3, Duration.ofMillis(100))
                .filter { it is OAuth2ClientException }
                .doBeforeRetry {
                    log.info("Retry kall mot token endpoint feilet med  ${it.failure().message} for ${it.totalRetriesInARow() + 1} gang, prøver igjen",
                        it.failure())
                }
                .onRetryExhaustedThrow { _, spec ->
                    spec.failure()
                        .also { log.warn("Retry mot token endpoint gir opp etter ${spec.totalRetriesInARow()} forsøk") }
                }
    }

    companion object {

        private const val MASK = "******"
        fun ClientConfigurationProperties.clientCredentialFlow(service : OAuth2AccessTokenService, key : String) =
            ExchangeFilterFunction { req, next ->
                next.exchange(ClientRequest.from(req)
                    .header(AUTHORIZATION, service.bearerToken(registration[key.lowercase()], req.url())).build())
            }
    }
}