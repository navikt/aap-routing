package no.nav.aap.fordeling.graphql

import org.springframework.graphql.client.FieldAccessException
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.FORBIDDEN
import org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.HttpStatus.UNAUTHORIZED
import no.nav.aap.api.felles.error.IrrecoverableGraphQLException.BadGraphQLException
import no.nav.aap.api.felles.error.IrrecoverableGraphQLException.NotFoundGraphQLException
import no.nav.aap.api.felles.error.IrrecoverableGraphQLException.UnauthenticatedGraphQLException
import no.nav.aap.api.felles.error.IrrecoverableGraphQLException.UnauthorizedGraphQLException
import no.nav.aap.api.felles.error.RecoverableGraphQLException.UnhandledGraphQLException
import no.nav.aap.util.LoggerUtil

object GraphQLExtensions {

    private const val NOTAUTHORIZED = "unauthorized"
    private const val UNAUTHENTICATED = "unauthenticated"
    private const val BADREQUEST = "bad_request"
    private const val NOTFOUND = "not_found"

    private val log = LoggerUtil.getLogger(javaClass)

    fun FieldAccessException.oversett() = oversett(response.errors.firstOrNull()?.extensions?.get("code")?.toString(), message ?: "Ukjent feil").also { e ->
        log.warn("GraphQL oppslag returnerte ${response.errors.size} feil. ${response.errors}, oversatte feilkode til ${e.javaClass.simpleName}",
            this)
    }

    private fun oversett(kode : String?, msg : String) =
        when (kode) {
            NOTAUTHORIZED -> UnauthorizedGraphQLException(UNAUTHORIZED, msg)
            UNAUTHENTICATED -> UnauthenticatedGraphQLException(FORBIDDEN, msg)
            BADREQUEST -> BadGraphQLException(BAD_REQUEST, msg)
            NOTFOUND -> NotFoundGraphQLException(NOT_FOUND, msg)
            else -> UnhandledGraphQLException(INTERNAL_SERVER_ERROR, msg)
        }
}