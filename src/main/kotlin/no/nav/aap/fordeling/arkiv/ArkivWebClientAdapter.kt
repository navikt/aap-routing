package no.nav.aap.fordeling.arkiv

import graphql.kickstart.spring.webclient.boot.GraphQLWebClient
import no.nav.aap.fordeling.arkiv.Fordeler.FordelingResultat
import no.nav.aap.fordeling.arkiv.JournalpostDTO.JournalførendeEnhet.Companion.AUTOMATISK_JOURNALFØRING
import no.nav.aap.fordeling.arkiv.JournalpostDTO.OppdaterJournalpostForespørsel
import no.nav.aap.fordeling.arkiv.JournalpostDTO.OppdaterJournalpostRespons
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

    fun hentJournalpost(journalpost: Long) = query<JournalpostDTO>(graphQL, JOURNALPOST_QUERY, journalpost.asIdent())?.tilJournalpost()

    fun oppdaterOgFerdigstill(journalpostId: String,data: OppdaterJournalpostForespørsel) =
        with(journalpostId) {
            oppdater(this, data)
            ferdigstill(this)
            FordelingResultat(this,"OK")
        }

    private fun oppdater(journalpostId: String, data: OppdaterJournalpostForespørsel) =
        webClient.put()
            .uri { b -> b.path(cf.oppdaterPath).build(journalpostId) }
            .contentType(APPLICATION_JSON)
            .bodyValue(data)
            .retrieve()
            .bodyToMono<OppdaterJournalpostRespons>()
            .retryWhen(cf.retrySpec(log))
            .doOnSuccess { log.info("Oppdatering av journalpost $journalpostId med $data OK (respons $it)") }
            .doOnError { t -> log.warn("Oppdatering av journalpost $journalpostId med $data feilet", t) }
            .block()


     private fun ferdigstill(journalpostId: String) =
        webClient.patch()
            .uri { b -> b.path(cf.ferdigstillPath).build(journalpostId) }
            .contentType(APPLICATION_JSON)
            .bodyValue(AUTOMATISK_JOURNALFØRING)
            .retrieve()
            .bodyToMono<Any>()
            .retryWhen(cf.retrySpec(log))
            .doOnSuccess { log.info("Ferdigstilling av journalpost OK (respons $it)") }
            .doOnError { t -> log.warn("Ferdigstilling av journalpost $journalpostId feilet", t) }
            .block()


    companion object {
        private fun Long.asIdent() = mapOf(ID to "$this")
        private const val JOURNALPOST_QUERY = "query-journalpost.graphql"
        private const val ID = "journalpostId"
    }
}