package no.nav.aap.fordeling.config

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.micrometer.core.instrument.MeterRegistry
import java.time.Duration
import java.util.function.Consumer
import no.nav.aap.rest.AbstractWebClientAdapter.Companion.correlatingFilterFunction
import no.nav.aap.util.LoggerUtil.getLogger
import no.nav.boot.conditionals.ConditionalOnNotProd
import no.nav.boot.conditionals.ConditionalOnProd
import no.nav.security.token.support.client.core.OAuth2ClientException
import no.nav.security.token.support.client.core.http.OAuth2HttpClient
import no.nav.security.token.support.client.core.http.OAuth2HttpRequest
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenResponse
import no.nav.security.token.support.client.spring.oauth2.ClientConfigurationPropertiesMatcher
import org.springframework.boot.web.reactive.function.client.WebClientCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import reactor.util.retry.Retry.fixedDelay
import reactor.netty.transport.logging.AdvancedByteBufFormat.TEXTUAL
import io.netty.handler.logging.LogLevel.TRACE
import io.swagger.v3.oas.annotations.info.License
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer
import org.springframework.boot.info.BuildProperties

@Configuration
class GlobalBeanConfig(@Value("\${spring.application.name}") private val applicationName: String)  {

    @Bean
    fun swagger(p: BuildProperties): OpenAPI {
        return OpenAPI()
            .info(Info()
                .title("AAP fss proxy")
                .description("Proxy mot tjenester som ikke støtter AAD/TokenX")
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
        }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private interface IgnoreUnknown

    @Bean
    fun objectMapperCustomizer() = Jackson2ObjectMapperBuilderCustomizer {
        it.apply {
            mixIn(OAuth2AccessTokenResponse::class.java,IgnoreUnknown::class.java)
            modules(JavaTimeModule(), KotlinModule.Builder().build()) }
    }

    @ConditionalOnNotProd
    @Bean
    fun notProdHttpClient() = HttpClient.create().wiretap(javaClass.name, TRACE, TEXTUAL)

    @ConditionalOnProd
    @Bean
    fun prodHttpClient() = HttpClient.create()
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
                    .onErrorMap { e -> e as? OAuth2ClientException ?: OAuth2ClientException("Uventet feil fra token endpoint $tokenEndpointUrl",e) }
                    .doOnSuccess { log.trace("Token endpoint $tokenEndpointUrl returnerte OK") }
                    .block()
                    ?: throw OAuth2ClientException("Ingen respons (null) fra token endpoint $tokenEndpointUrl")
            }
        private fun retry() =
            fixedDelay(3, Duration.ofMillis(100))
                .filter { it is OAuth2ClientException }
                .doBeforeRetry { log.info("Retry kall mot token endpoint feilet med  ${it.failure().message} for ${it.totalRetriesInARow() + 1} gang, prøver igjen",it.failure()) }
                .onRetryExhaustedThrow { _, spec ->  spec.failure().also { log.warn("Retry mot token endpoint gir opp etter ${spec.totalRetriesInARow()} forsøk") } }

    }
}