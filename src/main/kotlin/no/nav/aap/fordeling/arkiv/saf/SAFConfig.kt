package no.nav.aap.fordeling.arkiv.saf

import java.net.URI
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.web.util.UriBuilder
import no.nav.aap.fordeling.fordeling.FordelingDTOs.DokumentVariant.VariantFormat
import no.nav.aap.fordeling.arkiv.saf.SAFConfig.Companion.SAF
import no.nav.aap.rest.AbstractRestConfig
import no.nav.aap.rest.AbstractRestConfig.RetryConfig.Companion.DEFAULT

@ConfigurationProperties(SAF)
class SAFConfig(baseUri : URI, val dokPath : String = DOK_PATH, enabled : Boolean = true) : AbstractRestConfig(baseUri, "", SAF, enabled, DEFAULT) {

    fun dokUri(b : UriBuilder, id : String, dokumentId : String, variantFormat : VariantFormat) =
        b.path(dokPath).build(id, dokumentId, variantFormat.name)

    companion object {

        private const val DOK_PATH = "/rest/hentdokument/{journalpostId}/{dokumentInfoId}/{variantFormat}"
        const val SAF = "saf"
        const val SAFDOK = "doksaf"
    }
}