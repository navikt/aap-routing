package no.nav.aap.fordeling.arkiv

import jakarta.validation.constraints.NotEmpty
import no.nav.aap.fordeling.arkiv.Fordeler.Companion.INGEN_FORDELER
import no.nav.aap.fordeling.arkiv.FordelerKonfig.Companion.FORDELING
import no.nav.aap.util.LoggerUtil.getLogger
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(FORDELING)
data class FordelerKonfig(val topic: FordelerTopics,val routing: @NotEmpty Map<String, FordelingProperties>) {
    val log = getLogger(javaClass)

    fun fordelerFor(jp: Journalpost, fordelere: List<Fordeler>) =
        routing[jp.tema]?.let { c ->
            if (jp.dokumenter.any { it.brevkode in c.brevkoder }) {  //2b kandidat for automatisk journalføring
                fordelere.firstOrNull { jp.tema in it.tema() }
            } else {
                INGEN_FORDELER.also {// 2a TODO må vel kaste exception her?
                    log.info("Journalpost $jp med ${jp.tema} fordeles ikke")
                }
            }
        } ?: INGEN_FORDELER.also {
            log.info("Ingen konfigurasjon for ${jp.tema}")
        }


    data class FordelerTopics(val retry: String, val dlt: String, val backoff: Int, val retries: Int)
    data class FordelingProperties(val brevkoder: List<String>)

    companion object {
        const val FORDELING = "fordeling"
    }
}