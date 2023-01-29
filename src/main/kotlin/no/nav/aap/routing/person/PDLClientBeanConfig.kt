package no.nav.aap.routing.person

import com.fasterxml.jackson.databind.ObjectMapper
import graphql.kickstart.spring.webclient.boot.GraphQLWebClient
import no.nav.aap.health.AbstractPingableHealthIndicator
import no.nav.aap.rest.AbstractWebClientAdapter.Companion.temaFilterFunction
import no.nav.aap.routing.person.PDLConfig.Companion.PDL
import no.nav.aap.util.TokenExtensions.bearerToken
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenService
import no.nav.security.token.support.client.spring.ClientConfigurationProperties
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.http.HttpHeaders.*
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClient.Builder

@Bean
@Qualifier(PDL)
fun pdlWebClient(b: Builder, cfg: PDLConfig, @Qualifier(PDL) pdlClientCredentialFilterFunction: ExchangeFilterFunction) =
    b.baseUrl("${cfg.baseUri}")
        .filter(temaFilterFunction())
        .filter(pdlClientCredentialFilterFunction)
        .build()

@Bean
@Qualifier(PDL)
fun pdlClientCredentialFilterFunction(cfgs: ClientConfigurationProperties, service: OAuth2AccessTokenService) =
    ExchangeFilterFunction { req, next ->
        next.exchange(ClientRequest.from(req).header(AUTHORIZATION, service.bearerToken(cfgs.registration[PDL], req.url())).build())
    }

@Qualifier(PDL)
@Bean
fun pdlGraphQLClient(@Qualifier(PDL) client: WebClient, mapper: ObjectMapper) = GraphQLWebClient.newInstance(client, mapper)

@Bean
@ConditionalOnProperty("$PDL.enabled", havingValue = "true")
fun pdlHealthIndicator(a: PDLWebClientAdapter) = object : AbstractPingableHealthIndicator(a) {}
}