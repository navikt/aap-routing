package no.nav.aap.fordeling.graphql

import graphql.kickstart.spring.webclient.boot.GraphQLErrorsException
import no.nav.aap.api.felles.error.IrrecoverableIntegrationException
import no.nav.aap.api.felles.error.RecoverableIntegrationException
import no.nav.aap.fordeling.graphql.GraphQLExtensions.IrrecoverableGraphQLException.BadGraphQL
import no.nav.aap.fordeling.graphql.GraphQLExtensions.IrrecoverableGraphQLException.NotFoundGraphQL
import no.nav.aap.fordeling.graphql.GraphQLExtensions.IrrecoverableGraphQLException.UnauthenticatedGraphQL
import no.nav.aap.fordeling.graphql.GraphQLExtensions.IrrecoverableGraphQLException.UnauthorizedGraphQL
import no.nav.aap.fordeling.graphql.GraphQLExtensions.RecoverableGraphQLException.UnhandledGraphQL
import no.nav.aap.util.LoggerUtil
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.FORBIDDEN
import org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.HttpStatus.UNAUTHORIZED

object GraphQLExtensions {

    private const val NOTAUTHORIZED = "unauthorized"
    private const val UNAUTHENTICATED = "unauthenticated"
    private const val BADREQUEST = "bad_request"
    private const val NOTFOUND = "not_found"

    private val log = LoggerUtil.getLogger(javaClass)

    fun GraphQLErrorsException.oversett() = oversett(code(), message ?: "Ukjent feil").also {
        log.warn("GraphQL oppslag returnerte ${errors.size} feil. ${errors}, oversatte feilkode til ${it.javaClass.simpleName}",
                this)
    }

    private fun GraphQLErrorsException.code() = errors.firstOrNull()?.extensions?.get("code")?.toString()

    private fun oversett(kode: String?, msg: String) =
        when (kode) {
            NOTAUTHORIZED -> UnauthorizedGraphQL(UNAUTHORIZED, msg)
            UNAUTHENTICATED -> UnauthenticatedGraphQL(FORBIDDEN, msg)
            BADREQUEST -> BadGraphQL(BAD_REQUEST, msg)
            NOTFOUND -> NotFoundGraphQL(NOT_FOUND, msg)
            else -> UnhandledGraphQL(INTERNAL_SERVER_ERROR, msg)
        }

    abstract class IrrecoverableGraphQLException(status: HttpStatus, msg: String) : IrrecoverableIntegrationException("$msg (${status.value()})", null,null) {
        class NotFoundGraphQL(status: HttpStatus, msg: String) : IrrecoverableGraphQLException(status, msg)
        class BadGraphQL(status: HttpStatus, msg: String) : IrrecoverableGraphQLException(status, msg)
        class UnauthenticatedGraphQL(status: HttpStatus, msg: String) : IrrecoverableGraphQLException(status, msg)
        class UnauthorizedGraphQL(status: HttpStatus, msg: String) : IrrecoverableGraphQLException(status, msg)

    }

    abstract class RecoverableGraphQLException(status: HttpStatus, msg: String) : RecoverableIntegrationException("${status.value()}-$msg", null) {
        class UnhandledGraphQL(status: HttpStatus, msg: String) : RecoverableGraphQLException(status, msg)
    }
}