package no.nav.aap.fordeling.arkiv.fordeling

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonValue
import org.springframework.kafka.support.KafkaHeaders.TOPIC
import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.fordeling.arena.ArenaDTOs.ArenaOpprettOppgaveData
import no.nav.aap.fordeling.arkiv.fordeling.Fordeler.FordelingResultat.FordelingType
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.DokumentVariant.VariantFormat
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.DokumentVariant.VariantFormat.ORIGINAL
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.JournalpostDTO.Kanal
import no.nav.aap.fordeling.arkiv.fordeling.Journalpost.Bruker
import no.nav.aap.fordeling.navenhet.NAVEnhet
import no.nav.aap.fordeling.person.Diskresjonskode
import no.nav.aap.fordeling.person.Diskresjonskode.ANY
import no.nav.aap.fordeling.util.MetrikkKonstanter.BREVKODE
import no.nav.aap.fordeling.util.MetrikkKonstanter.FORDELINGSTYPE
import no.nav.aap.fordeling.util.MetrikkKonstanter.FORDELINGTS
import no.nav.aap.fordeling.util.MetrikkKonstanter.KANAL
import no.nav.aap.fordeling.util.MetrikkKonstanter.TITTEL
import no.nav.aap.util.Constants.TEMA
import no.nav.aap.util.Metrikker

typealias AvsenderMottaker = Bruker

data class Journalpost(val id : String, val status : JournalpostStatus, val enhet : NAVEnhet?, val tittel : String?,
                       val tema : String, val behandlingstema : String?, val fnr : Fødselsnummer,
                       val bruker : Bruker?, val avsenderMottager : AvsenderMottaker?, val kanal : Kanal,
                       val dokumenter : Set<DokumentInfo> = emptySet()) {

    @JsonIgnore
    val egenAnsatt = bruker?.erEgenAnsatt ?: false

    @JsonIgnore
    val diskresjonskode = bruker?.diskresjonskode ?: ANY

    @JsonValue
    val hovedDokument = dokumenter.first()

    @JsonIgnore
    val hovedDokumentBrevkode = hovedDokument.brevkode ?: "Ukjent brevkode"

    @JsonIgnore
    val hovedDokumentId = hovedDokument.id

    @JsonIgnore
    val hovedDokumentTittel = hovedDokument.tittel ?: "Ukjent tittel"

    @JsonIgnore
    val vedleggTitler = dokumenter.drop(1).mapNotNull { it.tittel }

    fun erMeldekort() = tittel?.contains("Meldekort", true) ?: false

    // TODO Denne er på feil sted
    fun opprettArenaOppgaveData(enhet : NAVEnhet) = ArenaOpprettOppgaveData(fnr, enhet.enhetNr, hovedDokumentTittel, vedleggTitler)

    fun metrikker(type : FordelingType, topic : String) =
        Metrikker.inc(FORDELINGTS, listOf(
            Pair(TEMA, tema),
            Pair(TOPIC, topic),
            Pair(FORDELINGSTYPE, type.name),
            Pair(TITTEL, tittel ?: "Ukjent tittel"),
            Pair(KANAL, kanal),
            Pair(BREVKODE, hovedDokumentBrevkode)))

    data class Bruker(val fnr : Fødselsnummer, val diskresjonskode : Diskresjonskode = ANY, val erEgenAnsatt : Boolean = false)

    data class DokumentInfo(val id : String, val tittel : String?, val brevkode : String?, val dokumentVarianter : List<VariantFormat>) {

        fun harOriginal() = dokumentVarianter.contains(ORIGINAL)
    }

    enum class JournalpostStatus { MOTTATT, JOURNALFØRT, UKJENT }
}