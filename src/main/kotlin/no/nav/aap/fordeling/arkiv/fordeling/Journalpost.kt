package no.nav.aap.fordeling.arkiv.fordeling

import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.kafka.support.KafkaHeaders.TOPIC
import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.fordeling.arena.ArenaDTOs.ArenaOpprettOppgaveData
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.FordelingResultat.FordelingType
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.JournalpostDTO.Bruker
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.JournalpostDTO.DokumentInfo
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.JournalpostDTO.JournalStatus
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.JournalpostDTO.JournalpostType
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.JournalpostDTO.Kanal
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.JournalpostDTO.RelevantDato
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.JournalpostDTO.Tilleggsopplysning
import no.nav.aap.fordeling.navenhet.EnhetsKriteria.NavOrg.NAVEnhet
import no.nav.aap.fordeling.person.Diskresjonskode.ANY
import no.nav.aap.fordeling.util.MetrikkLabels.BREVKODE
import no.nav.aap.fordeling.util.MetrikkLabels.FORDELINGSTYPE
import no.nav.aap.fordeling.util.MetrikkLabels.FORDELINGTS
import no.nav.aap.fordeling.util.MetrikkLabels.KANAL
import no.nav.aap.fordeling.util.MetrikkLabels.TITTEL
import no.nav.aap.util.Constants.TEMA
import no.nav.aap.util.Metrikker

typealias AvsenderMottaker = Bruker

data class Journalpost(val tittel : String?, val journalførendeEnhet : String?, val journalpostId : String, val status : JournalStatus,
                       val type : JournalpostType, val tema : String, val behandlingstema : String?, val fnr : Fødselsnummer,
                       val bruker : Bruker?, val avsenderMottager : AvsenderMottaker?, val kanal : Kanal, val relevanteDatoer : Set<RelevantDato> = emptySet(),
                       val dokumenter : Set<DokumentInfo> = emptySet(), val tilleggsopplysninger : Set<Tilleggsopplysning> = emptySet()) {

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

    fun opprettArenaOppgaveData(enhet : NAVEnhet) =
        ArenaOpprettOppgaveData(fnr, enhet.enhetNr, hovedDokumentTittel, vedleggTitler)

    fun metrikker(type : FordelingType, topic : String) =
        Metrikker.inc(FORDELINGTS, listOf(
            Pair(TEMA, tema),
            Pair(TOPIC, topic),
            Pair(FORDELINGSTYPE, type.name),
            Pair(TITTEL, tittel ?: "Ukjent tittel"),
            Pair(KANAL, kanal),
            Pair(BREVKODE, hovedDokumentBrevkode)))
}