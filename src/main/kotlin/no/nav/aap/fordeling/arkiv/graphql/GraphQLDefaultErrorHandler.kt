package no.nav.aap.fordeling.arkiv.graphql

import graphql.kickstart.spring.webclient.boot.GraphQLErrorsException
import no.nav.aap.fordeling.arkiv.graphql.GraphQLExtensions.oversett
import org.springframework.stereotype.Component

@Component
class GraphQLDefaultErrorHandler : GraphQLErrorHandler {

     override fun handle(e: Throwable): Nothing {
         when (e) {
             is GraphQLErrorsException ->  throw e.oversett()
             else -> throw e
         }
    }
}