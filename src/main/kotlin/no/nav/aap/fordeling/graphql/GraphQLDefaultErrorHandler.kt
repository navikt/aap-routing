package no.nav.aap.fordeling.graphql

import graphql.kickstart.spring.webclient.boot.GraphQLErrorsException
import no.nav.aap.api.felles.error.IrrecoverableIntegrationException
import no.nav.aap.api.felles.error.RecoverableIntegrationException
import no.nav.aap.fordeling.graphql.GraphQLExtensions.RecoverableGraphQLException
import no.nav.aap.fordeling.graphql.GraphQLExtensions.RecoverableGraphQLException.UnhandledGraphQL
import no.nav.aap.fordeling.graphql.GraphQLExtensions.oversett
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import org.springframework.stereotype.Component

@Component
class GraphQLDefaultErrorHandler : GraphQLErrorHandler {

    private val log = LoggerFactory.getLogger(GraphQLDefaultErrorHandler::class.java)

    override fun handle(e: Throwable): Nothing {
        when (e) {
            is GraphQLErrorsException -> throw e.oversett()
            else -> throw UnhandledGraphQL(INTERNAL_SERVER_ERROR,"GraphQL oppslag feilet", e)
        }
    }
}