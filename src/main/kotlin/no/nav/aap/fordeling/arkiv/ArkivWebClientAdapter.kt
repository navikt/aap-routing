package no.nav.aap.fordeling.arkiv

import graphql.kickstart.spring.webclient.boot.GraphQLWebClient
import no.nav.aap.fordeling.arkiv.ArkivConfig.Companion.DOKARKIV
import no.nav.aap.fordeling.arkiv.Fordeler.FordelingResultat
import no.nav.aap.fordeling.arkiv.JournalpostDTO.JournalførendeEnhet.Companion.AUTOMATISK_JOURNALFØRING
import no.nav.aap.fordeling.arkiv.JournalpostDTO.OppdaterForespørsel
import no.nav.aap.fordeling.arkiv.JournalpostDTO.OppdaterRespons
import no.nav.aap.fordeling.arkiv.JournalpostDTO.OppdaterRespons.Companion.EMPTY
import no.nav.aap.fordeling.arkiv.graphql.AbstractGraphQLAdapter
import no.nav.aap.util.Constants.JOARK
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.MediaType.*
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

@Component
class ArkivWebClientAdapter(@Qualifier(JOARK) private val graphQL: GraphQLWebClient, @Qualifier(JOARK) webClient: WebClient, @Qualifier(DOKARKIV) private val dokarkiv: WebClient, val cf: ArkivConfig) :
    AbstractGraphQLAdapter(webClient, cf) {

    fun hentJournalpost(journalpostId: Long) = query<JournalpostDTO>(graphQL, JOURNALPOST_QUERY, journalpostId.asIdent())?.tilJournalpost()

    fun oppdaterOgFerdigstillJournalpost(journalpostId: String, data: OppdaterForespørsel) =
        with(journalpostId) {
            oppdaterJournalpost(this, data)
            ferdigstillJournalpost(this)
            FordelingResultat(this,"OK")
        }

    fun oppdaterJournalpost(journalpostId: String, data: OppdaterForespørsel) =
        if (cf.isEnabled) {
            dokarkiv.put()
                .uri { b -> b.path(cf.oppdaterPath).build(journalpostId) }
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .bodyValue(data)
                .retrieve()
                .bodyToMono<OppdaterRespons>()
                .retryWhen(cf.retrySpec(log))
                .doOnSuccess { log.info("Oppdatering av journalpost $journalpostId med $data OK. Respons $it") }
                .doOnError { t -> log.warn("Oppdatering av journalpost $journalpostId med $data feilet", t) }
                .block()
        }
        else {
            EMPTY.also {
                log.info("Oppdaterte ikke journalpost med $data")
            }
        }



    fun ferdigstillJournalpost(journalpostId: String) =
        if (cf.isEnabled) {
            dokarkiv.patch()
            .uri { b -> b.path(cf.ferdigstillPath).build(journalpostId) }
            .contentType(APPLICATION_JSON)
            .accept(TEXT_PLAIN)
            .bodyValue(AUTOMATISK_JOURNALFØRING)
            .retrieve()
            .bodyToMono<String>()
            .retryWhen(cf.retrySpec(log))
            .doOnSuccess { log.info("Ferdigstilling av journalpost OK. Respons $it") }
            .doOnError { t -> log.warn("Ferdigstilling av journalpost $journalpostId feilet", t) }
            .block()
        }
        else {
            "Ferdigstilte ikke journalpost $journalpostId".also {
                log.info(it)
            }
        }


    companion object {
        private fun Long.asIdent() = mapOf(ID to "$this")
        private const val JOURNALPOST_QUERY = "query-journalpost.graphql"
        private const val ID = "journalpostId"
    }
}