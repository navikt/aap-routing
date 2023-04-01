package no.nav.aap.fordeling.arkiv.fordeling

import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.kafka.support.KafkaHeaders.TOPIC
import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.fordeling.arena.ArenaDTOs.ArenaOpprettOppgaveData
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.FordelingResultat.FordelingType
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.JournalpostDTO.BrukerDTO
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.JournalpostDTO.BrukerDTO.BrukerTypeDTO.FNR
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.JournalpostDTO.DokumentInfoDTO
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

data class Journalpost(val tittel : String?, val enhet : NAVEnhet?, val journalpostId : String, val status : JournalpostStatus,
                       val tema : String, val behandlingstema : String?, val fnr : Fødselsnummer,
                       val bruker : Bruker?, val avsenderMottager : AvsenderMottaker?, val kanal : Kanal,
                       val dokumenter : Set<DokumentInfoDTO> = emptySet()) {

    @JsonIgnore
    val egenAnsatt = bruker?.erEgenAnsatt ?: false

    @JsonIgnore
    val diskresjonskode = bruker?.diskresjonskode ?: ANY

    @JsonIgnore
    val hovedDokumentBrevkode = dokumenter.firstOrNull()?.brevkode ?: "Ukjent brevkode"

    @JsonIgnore
    val hovedDokumentTittel = dokumenter.firstOrNull()?.tittel ?: "Ukjent tittel"

    @JsonIgnore
    val vedleggTitler = dokumenter.drop(1).mapNotNull { it.tittel }

    fun erMeldekort() = tittel?.contains("Meldekort", true) ?: false

    fun opprettArenaOppgaveData(enhet : NAVEnhet) = ArenaOpprettOppgaveData(fnr, enhet.enhetNr, hovedDokumentTittel, vedleggTitler)

    fun metrikker(type : FordelingType, topic : String) =
        Metrikker.inc(FORDELINGTS, listOf(
            Pair(TEMA, tema),
            Pair(TOPIC, topic),
            Pair(FORDELINGSTYPE, type.name),
            Pair(TITTEL, tittel ?: "Ukjent tittel"),
            Pair(KANAL, kanal),
            Pair(BREVKODE, hovedDokumentBrevkode)))

    data class Bruker(val fnr : Fødselsnummer, val diskresjonskode : Diskresjonskode = ANY, val erEgenAnsatt : Boolean = false) {

        fun tilDTO() = BrukerDTO(fnr.fnr, FNR)
    }

    enum class JournalpostStatus { MOTTATT, JOURNALFØRT, UKJENT }
}