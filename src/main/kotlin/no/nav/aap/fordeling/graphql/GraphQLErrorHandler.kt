package no.nav.aap.fordeling.graphql

interface GraphQLErrorHandler {

    fun handle(e : Throwable, query : String) : Nothing
}