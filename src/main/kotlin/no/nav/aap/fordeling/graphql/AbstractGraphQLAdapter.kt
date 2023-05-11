package no.nav.aap.fordeling.graphql

import org.slf4j.LoggerFactory
import org.springframework.graphql.client.ClientGraphQlRequest
import org.springframework.graphql.client.GraphQlClient
import org.springframework.graphql.client.GraphQlClientInterceptor
import org.springframework.graphql.client.GraphQlClientInterceptor.Chain
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.http.MediaType.TEXT_PLAIN
import org.springframework.web.reactive.function.client.WebClient
import no.nav.aap.api.felles.graphql.GraphQLDefaultErrorHandler
import no.nav.aap.api.felles.graphql.GraphQLErrorHandler
import no.nav.aap.rest.AbstractRestConfig
import no.nav.aap.rest.AbstractWebClientAdapter

abstract class AbstractGraphQLAdapter(client : WebClient, protected val graphQL : GraphQlClient, cfg : AbstractRestConfig,
                                      val handler : GraphQLErrorHandler = GraphQLDefaultErrorHandler()) :
    AbstractWebClientAdapter(client, cfg) {

    protected inline fun <reified T> query(query : Pair<String, String>, vars : Map<String, String>, info : String) =
        runCatching {
            graphQL
                .documentName(query.first)
                .variables(vars)
                .retrieve(query.second)
                .toEntity(T::class.java)
                .contextCapture()
                .block().also {
                    log.trace("Slo opp {} {}", T::class.java.simpleName, it)
                }
        }.getOrElse { t ->
            log.warn("Query $query feilet. $info", t)
            handler.handle(t)
        }

    override fun ping() =
        webClient
            .options()
            .uri(baseUri)
            .accept(APPLICATION_JSON, TEXT_PLAIN)
            .retrieve()
            .toBodilessEntity()
            .contextCapture()
            .block().run { emptyMap<String, String>() }

    override fun toString() = "handler=$handler,graphQL=$graphQL"
}

class LoggingGraphQLInterceptor : GraphQlClientInterceptor {

    private val log = LoggerFactory.getLogger(LoggingGraphQLInterceptor::class.java)

    override fun intercept(req : ClientGraphQlRequest, chain : Chain) = chain.next(req).also {
        log.trace("Eksekverer query {} {}", req.operationName, req.document)
    }
}