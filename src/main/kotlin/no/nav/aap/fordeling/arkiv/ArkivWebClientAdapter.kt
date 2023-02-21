package no.nav.aap.fordeling.arkiv

import graphql.kickstart.spring.webclient.boot.GraphQLWebClient
import no.nav.aap.fordeling.arkiv.JournalpostDTO.OppdateringData
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

    fun oppdaterOgFerdigstill(journalpost: Journalpost, saksNr: String, enhetNr: String)  {
        oppdater(journalpost.oppdateringsData(saksNr,enhetNr), journalpost.journalpostId)
        ferdigstill(journalpost)
    }
    fun oppdater(data: OppdateringData, id: String) =
        webClient.put()
            .uri { b -> b.path(cf.oppdaterPath).build(id) }
            .contentType(APPLICATION_JSON)
            .bodyValue(data)
            .retrieve()
            .bodyToMono<Any>()
            .retryWhen(cf.retrySpec(log))
            .doOnSuccess { log.info("Oppdatering av journalpost OK ($it)") }
            .doOnError { t -> log.warn("Oppdatering av journalpost $data feilet", t) }
            .block()


    private fun ferdigstill(jp: Journalpost) = Unit

    companion object {
        private fun Long.asIdent() = mapOf(ID to "$this")
        private const val JOURNALPOST_QUERY = "query-journalpost.graphql"
        private const val ID = "journalpostId"
    }
}