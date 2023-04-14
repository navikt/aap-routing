package no.nav.aap.fordeling.graphql

import com.fasterxml.jackson.databind.exc.MismatchedInputException
import org.springframework.graphql.client.GraphQlClientException
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import no.nav.aap.api.felles.error.IrrecoverableIntegrationException
import no.nav.aap.fordeling.graphql.GraphQLExtensions.IrrecoverableGraphQLException.UnexpectedResponseGraphQLException
import no.nav.aap.fordeling.graphql.GraphQLExtensions.RecoverableGraphQLException.UnhandledGraphQLException

class BootGraphQLDefaultErrorHandler : GraphQLErrorHandler {

    override fun handle(e : Throwable, query : String) : Nothing {
        when (e) {
            is MismatchedInputException -> throw UnexpectedResponseGraphQLException(BAD_REQUEST, "Ikke-håndtert respons for  $query")
            is GraphQlClientException -> throw IrrecoverableIntegrationException(e.message, cause = e)
            else -> throw UnhandledGraphQLException(INTERNAL_SERVER_ERROR, "GraphQL oppslag $query feilet", e)
        }
    }
}