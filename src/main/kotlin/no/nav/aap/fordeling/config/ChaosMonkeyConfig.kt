package no.nav.aap.fordeling.config

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.config.MeterFilter
import io.micrometer.core.instrument.config.MeterFilter.*
import io.netty.channel.ChannelOption.CONNECT_TIMEOUT_MILLIS
import io.netty.handler.logging.LogLevel.*
import io.netty.handler.timeout.WriteTimeoutHandler
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.License
import java.time.Duration
import java.time.Duration.ofSeconds
import java.util.*
import java.util.concurrent.TimeUnit.*
import kotlin.random.Random.Default.nextInt
import no.nav.aap.fordeling.util.MetrikkLabels.BREVKODE
import no.nav.aap.fordeling.util.MetrikkLabels.TITTEL
import no.nav.aap.rest.AbstractWebClientAdapter.Companion.chaosMonkeyRequestFilterFunction
import no.nav.aap.rest.AbstractWebClientAdapter.Companion.correlatingFilterFunction
import no.nav.aap.util.ChaosMonkey
import no.nav.aap.util.LoggerUtil.getLogger
import no.nav.aap.util.TokenExtensions.bearerToken
import no.nav.aap.util.WebClientExtensions.toResponse
import no.nav.boot.conditionals.Cluster
import no.nav.boot.conditionals.Cluster.*
import no.nav.boot.conditionals.Cluster.Companion.currentCluster
import no.nav.boot.conditionals.ConditionalOnNotProd
import no.nav.boot.conditionals.ConditionalOnProd
import no.nav.security.token.support.client.core.OAuth2ClientException
import no.nav.security.token.support.client.core.http.OAuth2HttpClient
import no.nav.security.token.support.client.core.http.OAuth2HttpRequest
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenResponse
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenService
import no.nav.security.token.support.client.spring.ClientConfigurationProperties
import no.nav.security.token.support.client.spring.oauth2.ClientConfigurationPropertiesMatcher
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer
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

@Configuration
class ChaosMonkeyConfig {

    private val log = getLogger(ChaosMonkeyConfig::class.java)

    @Bean
    @ConditionalOnNotProd
    fun notProdChaosMonkey() = ChaosMonkey(DEV_MONKEY)

    @Bean
    @ConditionalOnProd
    fun prodChaosMonkey() = ChaosMonkey(PROD_MONKEY)

    @Bean
    @ConditionalOnNotProd
    @Qualifier(MONKEY)
    fun notProdFilterMonkey() = chaosMonkeyRequestFilterFunction(DEV_FILTER_MONKEY)

    @Bean
    @ConditionalOnProd
    @Qualifier(MONKEY)
    fun prodFilterMonkey() = chaosMonkeyRequestFilterFunction(PROD_FILTER_MONKEY)


    companion object {

        const val MONKEY = "monkey"

        val NO_MONKEY = { false }

        val DEV_MONKEY = NO_MONKEY // monkey(DEV_GCP)

        val DEV_FILTER_MONKEY =  NO_MONKEY //monkey(DEV_GCP)

        val PROD_MONKEY = NO_MONKEY

        val PROD_FILTER_MONKEY =  NO_MONKEY //monkey(PROD_GCP)

        val LOCAL_MONKEY =   monkey(LOCAL)

        private fun monkey(vararg clusters: Cluster) = { -> nextInt(1, 5) == 1 && currentCluster in clusters.asList() }
    }
}