package no.nav.aap.fordeling.arkiv.dokarkiv

import java.net.URI
import no.nav.aap.fordeling.arkiv.dokarkiv.DokarkivConfig.Companion.DOKARKIV
import no.nav.aap.rest.AbstractRestConfig
import no.nav.aap.util.Constants.JOARK
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.DefaultValue
import org.springframework.web.util.UriBuilder

@ConfigurationProperties(DOKARKIV)
class DokarkivConfig(
        @DefaultValue(DEFAULT_PING_PATH) pingPath: String,
        @DefaultValue("false") enabled: Boolean,
        @DefaultValue(DOK_PATH) val dokPath: String,
        @DefaultValue(DEFAULT_FERDIGSTILL_PATH) val ferdigstillPath: String,
        @DefaultValue(DEFAULT_OPPDATER_PATH) val oppdaterPath: String,
        baseUri: URI) : AbstractRestConfig(baseUri, pingPath, JOARK, enabled) {

    fun dokUri(b: UriBuilder, journalpostId: String, dokumentInfoId:String, variantFormat: VariantFormat) = b.path(dokPath).build(journalpostId,dokumentInfoId,variantFormat.name)

    fun ferdigstillUri(b: UriBuilder, journalpostId: String) = b.path(ferdigstillPath).build(journalpostId)
    fun oppdaterJournlpostUri(b: UriBuilder, journalpostId: String) = b.path(oppdaterPath).build(journalpostId)
    override fun toString() = "${javaClass.simpleName} [pingPath=$pingPath,enabled=$isEnabled,baseUri=$baseUri]"

    companion object {
        const val DOKARKIV = "dokarkiv"
        private const val DOK_PATH = "/rest/hentdokument/{journalpostId}/{dokumentInfoId}/{variantFormat}"
        private const val PATH_PREFIX = "rest/journalpostapi/v1/journalpost/"
        private const val DEFAULT_FERDIGSTILL_PATH = "$PATH_PREFIX{journalpostid}/ferdigstill"
        private const val DEFAULT_OPPDATER_PATH = "$PATH_PREFIX{journalpostid}"
        private const val DEFAULT_PING_PATH = "actuator/health/liveness"
    }
}