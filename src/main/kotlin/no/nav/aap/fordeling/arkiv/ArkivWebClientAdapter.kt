package no.nav.aap.fordeling.arkiv

import graphql.kickstart.spring.webclient.boot.GraphQLWebClient
import no.nav.aap.fordeling.arkiv.graphql.AbstractGraphQLAdapter
import no.nav.aap.util.Constants.JOARK
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpStatus.*
import org.springframework.http.MediaType.*
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

@Component
class ArkivWebClientAdapter(@Qualifier(JOARK) private val graphQL: GraphQLWebClient, @Qualifier(JOARK) webClient: WebClient, val cf: ArkivConfig) :
    AbstractGraphQLAdapter(webClient, cf) {

    fun journalpost(journalpost: Long) = query<JournalpostDTO>(graphQL, JOURNALPOST_QUERY, journalpost.asIdent())?.tilJournalpost()

    private fun Long.asIdent() = mapOf(ID to "$this")
    fun oppdaterOgFerdigstill(jp: Journalpost, saksNr: String, enhetNr: String)  {
        oppdater(jp,saksNr,enhetNr)
        ferdigstill(jp)
    }
    private fun oppdater(jp: Journalpost, saksNr: String,enhetNr: String) =
        webClient.post()
            .uri { b -> b.path(cf.oppdaterPath).build(jp.journalpostId) }
            .contentType(APPLICATION_JSON)
            .bodyValue(jp.oppdateringsData(saksNr,enhetNr))
            .retrieve()
            .bodyToMono<Any>()
            .retryWhen(cf.retrySpec(log))
            .doOnError { t -> log.warn("Oppdatering av journalpost feilet", t) }
            .block()


    private fun ferdigstill(jp: Journalpost) = Unit

    companion object {
        private const val JOURNALPOST_QUERY = "query-journalpost.graphql"
        private const val ID = "journalpostId"
    }
}