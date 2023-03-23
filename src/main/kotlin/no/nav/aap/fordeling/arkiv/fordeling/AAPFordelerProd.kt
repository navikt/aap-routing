package no.nav.aap.fordeling.arkiv.fordeling

import no.nav.aap.api.felles.SkjemaType.STANDARD
import no.nav.aap.api.felles.SkjemaType.STANDARD_ETTERSENDING
import no.nav.aap.fordeling.arena.ArenaClient
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.FordelingResultat
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.FordelingResultat.FordelingType.AUTOMATISK
import no.nav.aap.fordeling.navenhet.EnhetsKriteria.NavOrg.NAVEnhet
import no.nav.aap.util.Constants.AAP
import no.nav.aap.util.LoggerUtil.getLogger
import no.nav.boot.conditionals.ConditionalOnProd
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Component

@Component
@ConditionalOnProd
@Primary
class AAPFordelerProd(
        private val arena: ArenaClient,
        private val manuell: AAPManuellFordelerProd) : Fordeler {

    val log = getLogger(AAPFordelerProd::class.java)
    override fun tema() = listOf(AAP)

    override fun fordelManuelt(jp: Journalpost, enhet: NAVEnhet) = manuell.fordelManuelt(jp,enhet)
    override fun fordel(jp: Journalpost, enhet: NAVEnhet) =
        runCatching {
            when (jp.hovedDokumentBrevkode) {
                STANDARD.kode -> {
                    log.info("Forsøker automatisk journalføring av ${jp.journalpostId} med brevkode ${jp.hovedDokumentBrevkode}")
                    fordelStandard(jp, enhet)
                }

                STANDARD_ETTERSENDING.kode -> {
                    log.info("Forsøker automatisk journalføring av ${jp.journalpostId} med brevkode ${jp.hovedDokumentBrevkode}")
                    fordelEttersending(jp)
                }

                else -> {
                    log.info("Brevkode ${jp.hovedDokumentBrevkode} IKKE konfigurert for automatisk fordeling for ${tema()}, forsøker manuelt")
                    manuell.fordel(jp, enhet)
                }
            }
        }.getOrElse {
            if (it !is ManuellFordelingException) {
                log.warn("Kunne IKKE automatisk fordele journalpost ${jp.journalpostId} (${jp.hovedDokumentBrevkode}), forsøker manuelt", it)
                manuell.fordel(jp, enhet)
            }
            else {
                log.info("Gjør IKKE umiddelbart nytt forsøk på manuell oppave siden manuelt forsøk akkurat feilet (${it.message})", it)
                throw it
            }
        }

    private fun fordelStandard(jp: Journalpost, enhet: NAVEnhet) =
        if (!arena.harAktivSak(jp.fnr)) {
            log.info("Arena har IKKE aktiv sak for ${jp.fnr}")
            FordelingResultat(AUTOMATISK, "Vellykket fordeling av ${jp.hovedDokumentBrevkode}", jp.hovedDokumentBrevkode, jp.journalpostId)
        }
        else {
            with("Har aktiv sak for ${jp.fnr}, skal IKKE opprett oppgave i Arena") {
                log.info(this)
                throw ArenaSakException(this)
            }
        }

    private fun fordelEttersending(jp: Journalpost) =
        arena.nyesteAktiveSak(jp.fnr)?.run {
            FordelingResultat(AUTOMATISK, "Vellykket fordeling", jp.hovedDokumentBrevkode, jp.journalpostId)
        } ?: throw ArenaSakException("Arena har IKKE aktiv sak for ${jp.fnr}, kan ikke oppdatere og ferdigstille journalpost").also {
            log.warn(it.message,it)
        }
}