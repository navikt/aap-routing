package no.nav.aap.fordeling.graphql

import graphql.kickstart.spring.webclient.boot.GraphQLErrorsException
import org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import org.springframework.stereotype.Component
import no.nav.aap.fordeling.graphql.GraphQLExtensions.RecoverableGraphQLException.UnhandledGraphQLException
import no.nav.aap.fordeling.graphql.GraphQLExtensions.oversett

@Component
class GraphQLDefaultErrorHandler : GraphQLErrorHandler {

    override fun handle(e : Throwable, query : String) : Nothing {
        when (e) {
            is GraphQLErrorsException -> throw e.oversett()
            else -> throw UnhandledGraphQLException(INTERNAL_SERVER_ERROR, "GraphQL oppslag $query feilet", e)
        }
    }
}