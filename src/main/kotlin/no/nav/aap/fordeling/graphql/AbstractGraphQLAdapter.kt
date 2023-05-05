package no.nav.aap.fordeling.graphql

import org.springframework.graphql.client.GraphQlClient
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.http.MediaType.TEXT_PLAIN
import org.springframework.web.reactive.function.client.WebClient
import no.nav.aap.rest.AbstractRestConfig
import no.nav.aap.rest.AbstractWebClientAdapter

abstract class AbstractGraphQLAdapter(client : WebClient, protected val graphQL : GraphQlClient, cfg : AbstractRestConfig,
                                      val handler : GraphQLErrorHandler = GraphQLDefaultErrorHandler()) :
    AbstractWebClientAdapter(client, cfg) {

    protected inline fun <reified T> query(query : String, path : String, vars : Map<String, String>, info : String) =
        runCatching {
            graphQL
                .documentName(query)
                .variables(vars)
                .retrieve(path)
                .toEntity(T::class.java)
                .block().also {
                    log.trace("Slo opp {} {}", T::class.java.simpleName, it)
                }
        }.getOrElse { t ->
            log.warn("Query $query feilet. $info", t)
            handler.handle(t, query)
        }

    override fun ping() =
        webClient
            .options()
            .uri(baseUri)
            .accept(APPLICATION_JSON, TEXT_PLAIN)
            .retrieve()
            .toBodilessEntity()
            .block().run { emptyMap<String, String>() }

    override fun toString() = "handler=$handler,graphQL=$graphQL"
}