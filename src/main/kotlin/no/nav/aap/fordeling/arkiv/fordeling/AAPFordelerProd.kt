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
import no.nav.boot.conditionals.Cluster
import no.nav.boot.conditionals.Cluster.Companion
import no.nav.boot.conditionals.Cluster.Companion.prodClusters
import no.nav.boot.conditionals.ConditionalOnNotProd
import no.nav.boot.conditionals.ConditionalOnProd
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Component

@Component
class AAPFordelerProd(
        private val arena: ArenaClient, arkiv: ArkivClient, manuell: ManuellFordelingFactory) : AAPFordeler(arena, arkiv,manuell) {

    val log = getLogger(AAPFordelerProd::class.java)

    override fun clusters() = prodClusters()  // For NOW

    override fun ferdigstillStandard(jp: Journalpost, enhet: NAVEnhet) = log.info("Liksom  ferdigstilling av søknad")
    override fun ferdigstillEttersending(jp: Journalpost, nyesteSak: String) = log.info("Liksom ordeler ferdigstilling av ettersending")
    override fun toString(): String {
        return "AAPFordelerProd(arena=$arena), tema=${tema()}, clusters=${clusters().asList()}"
    }
}