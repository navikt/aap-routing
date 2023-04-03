package no.nav.aap.fordeling.arkiv.dokarkiv

import java.net.URI
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.web.util.UriBuilder
import no.nav.aap.fordeling.arkiv.dokarkiv.DokarkivConfig.Companion.DOKARKIV
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.DokumentVariant.VariantFormat
import no.nav.aap.rest.AbstractRestConfig
import no.nav.aap.util.Constants.JOARK

@ConfigurationProperties(DOKARKIV)
class DokarkivConfig(baseUri : URI, enabled : Boolean = false, pingPath : String = DEFAULT_PING_PATH,
                     val dokPath : String = DOK_PATH, val ferdigstillPath : String = DEFAULT_FERDIGSTILL_PATH,
                     val oppdaterPath : String = DEFAULT_OPPDATER_PATH) : AbstractRestConfig(baseUri, pingPath, JOARK, enabled) {

    fun dokUri(b : UriBuilder, id : String, dokumentId : String, variantFormat : VariantFormat) =
        b.path(dokPath).build(id, dokumentId, variantFormat.name)

    fun ferdigstillUri(b : UriBuilder, id : String) = b.path(ferdigstillPath).build(id)

    fun oppdaterJournlpostUri(b : UriBuilder, id : String) = b.path(oppdaterPath).build(id)

    override fun toString() = "DokarkivConfig(dokPath='$dokPath', ferdigstillPath='$ferdigstillPath', oppdaterPath='$oppdaterPath')"

    companion object {

        const val DOKARKIV = "dokarkiv"
        private const val DOK_PATH = "/rest/hentdokument/{journalpostId}/{dokumentInfoId}/{variantFormat}"
        private const val PATH_PREFIX = "rest/journalpostapi/v1/journalpost/"
        private const val DEFAULT_FERDIGSTILL_PATH = "$PATH_PREFIX{journalpostid}/ferdigstill"
        private const val DEFAULT_OPPDATER_PATH = "$PATH_PREFIX{journalpostid}"
        private const val DEFAULT_PING_PATH = "actuator/health/liveness"
    }
}