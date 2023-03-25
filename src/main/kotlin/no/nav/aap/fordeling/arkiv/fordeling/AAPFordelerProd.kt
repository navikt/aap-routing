package no.nav.aap.fordeling.arkiv.fordeling

import no.nav.aap.fordeling.arena.ArenaClient
import no.nav.aap.fordeling.arkiv.ArkivClient
import no.nav.aap.fordeling.navenhet.EnhetsKriteria.NavOrg.NAVEnhet
import no.nav.aap.util.Constants.AAP
import no.nav.aap.util.LoggerUtil.getLogger
import no.nav.boot.conditionals.Cluster.Companion.prodClusters
import org.springframework.stereotype.Component

@Component
class AAPFordelerProd(
        private val arena: ArenaClient, arkiv: ArkivClient, manuell: ManuellFordelingFactory) : AAPFordeler(arena, arkiv,manuell) {

    val log = getLogger(AAPFordelerProd::class.java)

    override val cfg = FordelerConfig.of(prodClusters(),AAP)
    override fun ferdigstillStandard(jp: Journalpost, enhet: NAVEnhet) = log.info("Liksom  ferdigstilling av søknad")
    override fun ferdigstillEttersending(jp: Journalpost, nyesteSak: String) = log.info("Liksom ferdigstilling av ettersending")
    override fun toString(): String {
        return "AAPFordelerProd($arena=$arena), cfg=$cfg, manuell=${manuell}"
    }
}