package no.nav.aap.fordeling.graphql

import java.nio.charset.Charset.defaultCharset
import org.springframework.core.io.ClassPathResource
import org.springframework.graphql.client.HttpGraphQlClient
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.http.MediaType.TEXT_PLAIN
import org.springframework.web.reactive.function.client.WebClient
import no.nav.aap.rest.AbstractRestConfig
import no.nav.aap.rest.AbstractWebClientAdapter

abstract class BootAbstractGraphQLAdapter(client : WebClient, cfg : AbstractRestConfig, val handler : GraphQLErrorHandler = BootGraphQLDefaultErrorHandler()) :
    AbstractWebClientAdapter(client, cfg) {

    protected inline fun <reified T> query(graphQL : HttpGraphQlClient, query : String, args : Map<String, String>, info : String? = null) =
        runCatching {
            graphQL
                .document(ClassPathResource(query).getContentAsString(defaultCharset()))
                .variables(args)
                .retrieve("hentPerson")
                .toEntity(T::class.java).also {
                    log.trace("Slo opp {} {}", T::class.java.simpleName, it)
                }.block()
        }.getOrElse {
            log.warn("Query $query feilet. ${info?.let { " ($it)" } ?: ""}", it)
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