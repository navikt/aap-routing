package no.nav.aap.fordeling.graphql

import com.fasterxml.jackson.databind.exc.MismatchedInputException
import org.springframework.graphql.client.FieldAccessException
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import org.springframework.stereotype.Component
import no.nav.aap.api.felles.error.IrrecoverableGraphQLException.UnexpectedResponseGraphQLException
import no.nav.aap.api.felles.error.RecoverableGraphQLException.UnhandledGraphQLException
import no.nav.aap.fordeling.graphql.GraphQLExtensions.oversett

@Component
class GraphQLDefaultErrorHandler : GraphQLErrorHandler {

    override fun handle(e : Throwable, query : String) : Nothing {
        when (e) {
            is MismatchedInputException -> throw UnexpectedResponseGraphQLException(BAD_REQUEST, "Ikke-håndtert respons for  $query")
            is FieldAccessException -> throw e.oversett()
            else -> throw UnhandledGraphQLException(INTERNAL_SERVER_ERROR, "GraphQL oppslag $query feilet", e)
        }
    }
}