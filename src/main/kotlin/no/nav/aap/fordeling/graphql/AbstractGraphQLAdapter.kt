package no.nav.aap.fordeling.graphql

import graphql.kickstart.spring.webclient.boot.GraphQLWebClient
import no.nav.aap.rest.AbstractRestConfig
import no.nav.aap.rest.AbstractWebClientAdapter
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.http.MediaType.TEXT_PLAIN
import org.springframework.web.reactive.function.client.WebClient

abstract class AbstractGraphQLAdapter(client : WebClient, cfg : AbstractRestConfig, val handler : GraphQLErrorHandler = GraphQLDefaultErrorHandler()) :
    AbstractWebClientAdapter(client, cfg) {

    protected inline fun <reified T> query(graphQL : GraphQLWebClient, query : String, args : Map<String, String>, info : String? = null) =
        runCatching {
            graphQL.post(query, args, T::class.java).block().also {
                log.trace("Slo opp ${T::class.java.simpleName} $it")
            }
        }.getOrElse { t ->
            log.warn("Query $query feilet. ${info?.let { " ($it)" } ?: ""}", t)
            handler.handle(t, query)
        }

    protected inline fun <reified T> query(graphQL : GraphQLWebClient, query : String, args : Map<String, List<String>>, info : String? = null) =
        runCatching {
            graphQL.flux(query, args, T::class.java)
                .collectList().block()?.toList().also {
                    log.trace("Slo opp ${T::class.java.simpleName} $it")
                } ?: emptyList()
        }.getOrElse {
            log.warn("SAF bulk query $query feilet. ${info?.let { " ($it)" } ?: ""}", it)
            handler.handle(it, query)
        }

    override fun ping() =
        webClient
            .options()
            .uri(baseUri)
            .accept(APPLICATION_JSON, TEXT_PLAIN)
            .retrieve()
            .toBodilessEntity()
            .block().run { emptyMap<String, String>() }

    override fun toString() = "handler=$handler"
}