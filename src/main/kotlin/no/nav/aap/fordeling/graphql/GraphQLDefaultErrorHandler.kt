package no.nav.aap.fordeling.graphql

import graphql.kickstart.spring.webclient.boot.GraphQLErrorsException
import no.nav.aap.api.felles.error.IrrecoverableIntegrationException
import no.nav.aap.fordeling.graphql.GraphQLExtensions.oversett
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class GraphQLDefaultErrorHandler : GraphQLErrorHandler {

    private val log = LoggerFactory.getLogger(GraphQLDefaultErrorHandler::class.java)

    override fun handle(e: Throwable): Nothing {
        when (e) {
            is GraphQLErrorsException ->
                 e.oversett().also {
                     log.warn("GraphQL oppslag feilet, håndtert",it)
                     throw it
                 }
            else ->  e.also {
                log.warn("GraphQL oppslag feilet, ${it.javaClass.simpleName} ikke håndtert",it)
                throw IrrecoverableIntegrationException("GraphQL oppslag feilet",cause =it)
            }
        }
    }
}