package no.nav.aap.fordeling.arkiv.graphql

interface GraphQLErrorHandler {
    fun handle(e: Throwable): Nothing
}