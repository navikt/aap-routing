package no.nav.aap.fordeling.config

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.micrometer.core.instrument.MeterRegistry
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.License
import java.time.Duration
import java.util.function.Consumer
import kotlin.random.Random.Default.nextBoolean
import kotlin.random.Random.Default.nextInt
import no.nav.aap.api.felles.error.IntegrationException
import no.nav.aap.rest.AbstractWebClientAdapter.Companion.correlatingFilterFunction
import no.nav.aap.util.LoggerUtil.getLogger
import no.nav.aap.util.TokenExtensions.bearerToken
import no.nav.boot.conditionals.ConditionalOnNotProd
import no.nav.boot.conditionals.ConditionalOnProd
import no.nav.boot.conditionals.EnvUtil.isDevOrLocal
import no.nav.security.token.support.client.core.OAuth2ClientException
import no.nav.security.token.support.client.core.http.OAuth2HttpClient
import no.nav.security.token.support.client.core.http.OAuth2HttpRequest
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenResponse
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenService
import no.nav.security.token.support.client.spring.ClientConfigurationProperties
import no.nav.security.token.support.client.spring.oauth2.ClientConfigurationPropertiesMatcher
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer
import org.springframework.boot.info.BuildProperties
import org.springframework.boot.web.reactive.function.client.WebClientCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpHeaders.*
import org.springframework.http.HttpStatus.*
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.ExchangeFilterFunction.*
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono
import reactor.netty.http.client.HttpClient
import reactor.util.retry.Retry.fixedDelay

@Configuration
class GlobalBeanConfig(
        @Value("\${spring.application.name}") private val applicationName: String,
        private val env: Environment) {

    private val log = getLogger(GlobalBeanConfig::class.java)

    @Bean
    fun swagger(p: BuildProperties): OpenAPI {
        return OpenAPI()
            .info(Info()
                .title("AAP routng")
                .description("Routing ala KRUT (bare bedre)")
                .version(p.version)
                .license(License()
                    .name("MIT")
                    .url("https://nav.no"))
                 )
    }

    @Bean
    fun webClientCustomizer(client: HttpClient, registry: MeterRegistry) =
        WebClientCustomizer { b ->
            b.clientConnector(ReactorClientHttpConnector(client))
                .filter(correlatingFilterFunction(applicationName))
                .filter(faultInjectingRequestFilterFunction(env))
        }

    private fun faultInjectingRequestFilterFunction(env: Environment) =
        ofRequestProcessor {
            if (nextInt(1, 5) == 1 && isDevOrLocal(env) && !it.url().host.contains("microsoftonline")) {
                with(WebClientResponseException(BAD_GATEWAY,
                        "Tvunget feil for request til ${it.url()}",
                        null,
                        null,
                        null,
                        null)) {
                    log.info(message, this)
                    Mono.error(this)
                }
            }
            else {
                log.trace("Tvinger IKKE fram  feil for ${it.url()}")
                Mono.just(it)
            }
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
    fun notProdHttpClient() = HttpClient.create()//.wiretap(javaClass.name, TRACE, TEXTUAL)

    @ConditionalOnProd
    @Bean
    fun prodHttpClient() = HttpClient.create()

    @Bean
    fun configMatcher() =
        object : ClientConfigurationPropertiesMatcher {}

    @Bean
    fun retryingOAuth2HttpClient(b: WebClient.Builder) = RetryingWebClientOAuth2HttpClient(b.build())

    class RetryingWebClientOAuth2HttpClient(private val client: WebClient) : OAuth2HttpClient {

        private val log = getLogger(GlobalBeanConfig::class.java)

        override fun post(req: OAuth2HttpRequest) =
            with(req) {
                client.post()
                    .uri(tokenEndpointUrl)
                    .headers { Consumer<HttpHeaders> { it.putAll(oAuth2HttpHeaders.headers()) } }
                    .bodyValue(LinkedMultiValueMap<String, String>().apply { setAll(formParameters) })
                    .retrieve()
                    .bodyToMono(OAuth2AccessTokenResponse::class.java)
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

    @Component
    class FaultInjecter(private val env: Environment) {

        fun randomFeilHvisDev(component: Any) = env.randomFeilHvisDev(component)

        companion object {
            private val log = getLogger(FaultInjecter::class.java)
            private fun Environment.randomFeilHvisDev(component: Any) =
                if (isDevOrLocal(this)) {
                    if (nextBoolean()) {
                        throw IntegrationException("Dette er en tvunget feil i dev fra ${component.javaClass.simpleName}").also {
                            log.warn(it.message)
                        }
                    }
                    else {
                        Unit
                    }
                }
                else {
                    Unit
                }
        }
    }

    companion object {

        inline fun <T> T?.whenNull(block: T?.() -> Unit): T? {
            if (this == null) block()
            return this@whenNull
        }
        fun ClientConfigurationProperties.clientCredentialFlow(service: OAuth2AccessTokenService, key: String) =
            ExchangeFilterFunction { req, next ->
                next.exchange(ClientRequest.from(req)
                    .header(AUTHORIZATION, service.bearerToken(registration[key.lowercase()], req.url())).build())
            }
    }
}