package no.nav.aap.fordeling.arkiv.fordeling

import no.nav.aap.api.felles.SkjemaType.STANDARD
import no.nav.aap.api.felles.SkjemaType.STANDARD_ETTERSENDING
import no.nav.aap.fordeling.arena.ArenaClient
import no.nav.aap.fordeling.arkiv.ArkivClient
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.FordelingResultat
import no.nav.aap.fordeling.arkiv.fordeling.FordelingDTOs.FordelingResultat.FordelingType.AUTOMATISK
import no.nav.aap.fordeling.navenhet.EnhetsKriteria.NavOrg.NAVEnhet
import no.nav.aap.util.Constants.AAP
import no.nav.aap.util.LoggerUtil.getLogger
import no.nav.boot.conditionals.ConditionalOnNotProd
import no.nav.boot.conditionals.ConditionalOnProd
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Component

@Component
@Primary
@ConditionalOnNotProd
class AAPFordelerProd(
        private val arena: ArenaClient, arkiv: ArkivClient, manuell: AAPManuellFordelerProd) : AAPFordeler(arena, arkiv,manuell) {

    val log = getLogger(AAPFordelerProd::class.java)
    override fun ferdigstillStandard(jp: Journalpost, enhet: NAVEnhet) = log.info("Ingen ferdigstilling av søknad")
    override fun ferdigstillEttersending(jp: Journalpost, nyesteSak: String) = log.info("Ingen ferdigstilling av ettersending")
}