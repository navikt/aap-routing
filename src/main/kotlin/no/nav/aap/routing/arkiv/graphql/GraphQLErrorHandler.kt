package no.nav.aap.routing.arkiv.graphql

interface GraphQLErrorHandler {
    fun handle(e: Throwable): Nothing
}