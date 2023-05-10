package no.nav.aap.fordeling.arkiv.journalpost

import com.fasterxml.jackson.annotation.JsonIgnore
import java.util.UUID
import org.springframework.kafka.support.KafkaHeaders
import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.felles.SkjemaType
import no.nav.aap.fordeling.arkiv.journalpost.Journalpost.Bruker
import no.nav.aap.fordeling.fordeling.Fordeler
import no.nav.aap.fordeling.fordeling.FordelingDTOs
import no.nav.aap.fordeling.navenhet.NAVEnhet
import no.nav.aap.fordeling.person.Diskresjonskode
import no.nav.aap.fordeling.util.MetrikkKonstanter
import no.nav.aap.util.Constants
import no.nav.aap.util.Metrikker

typealias AvsenderMottaker = Bruker

data class Journalpost(val id : String, val status : JournalpostStatus, val enhet : NAVEnhet?, val tittel : String?,
                       val tema : String, val behandlingstema : String?, val fnr : Fødselsnummer,
                       val bruker : Bruker?, val avsenderMottager : AvsenderMottaker?, val kanal : FordelingDTOs.JournalpostDTO.Kanal,
                       val eksternReferanseId : UUID?,
                       val dokumenter : Set<DokumentInfo> = emptySet(), val tilleggsopplysninger : Set<Tilleggsopplysning> = emptySet()) {

    @JsonIgnore
    val egenAnsatt = bruker?.erEgenAnsatt ?: false

    @JsonIgnore
    val diskresjonskode = bruker?.diskresjonskode ?: Diskresjonskode.ANY

    @JsonIgnore
    val hovedDokument = dokumenter.first()

    @JsonIgnore
    val hovedDokumentId = hovedDokument.id

    @JsonIgnore
    val hovedDokumentBrevkode = hovedDokument.brevkode ?: "Ukjent brevkode"

    @JsonIgnore
    val hovedDokumentTittel = hovedDokument.tittel ?: "Ukjent tittel"

    @JsonIgnore
    val vedleggTitler = dokumenter.drop(1).mapNotNull { it.tittel }

    @JsonIgnore
    val erMeldekort = hovedDokumentBrevkode == SkjemaType.MELDEKORT.kode || tittel?.contains("Meldekort", true) ?: false

    val versjon = tilleggsopplysninger.find { it.nokkel == "versjon" }?.verdi

    @JsonIgnore
    val tilVikafossen = tilleggsopplysninger.find { it.nokkel == "routing" }?.verdi.toBoolean()

    fun metrikker(type : Fordeler.FordelingResultat.FordelingType, topic : String) =
        Metrikker.inc(
            MetrikkKonstanter.FORDELINGTS, listOf(
                Pair(Constants.TEMA, tema),
                Pair(KafkaHeaders.TOPIC, topic),
                Pair(MetrikkKonstanter.FORDELINGSTYPE, type.name),
                Pair(MetrikkKonstanter.TITTEL, tittel ?: "Ukjent tittel"),
                Pair(MetrikkKonstanter.KANAL, kanal),
                Pair(MetrikkKonstanter.BREVKODE, hovedDokumentBrevkode)
                                                 )
                     )

    data class Tilleggsopplysning(val nokkel : String, val verdi : String)

    data class Bruker(val fnr : Fødselsnummer, val diskresjonskode : Diskresjonskode = Diskresjonskode.ANY, val erEgenAnsatt : Boolean = false)

    data class DokumentInfo(val id : String, val tittel : String?, val brevkode : String?,
                            val dokumentVarianter : List<FordelingDTOs.DokumentVariant.VariantFormat>)

    enum class JournalpostStatus { MOTTATT, JOURNALFØRT, UKJENT }
}