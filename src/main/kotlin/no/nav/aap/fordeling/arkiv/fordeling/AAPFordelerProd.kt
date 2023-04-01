package no.nav.aap.fordeling.arkiv.fordeling

import org.springframework.stereotype.Component
import no.nav.aap.fordeling.arena.ArenaClient
import no.nav.aap.fordeling.arkiv.ArkivClient
import no.nav.aap.fordeling.arkiv.fordeling.Fordeler.FordelerConfig
import no.nav.aap.fordeling.arkiv.fordeling.Fordeler.FordelerConfig.Companion.PROD_AAP
import no.nav.aap.fordeling.navenhet.NAVEnhet
import no.nav.aap.util.LoggerUtil.getLogger

@Component
class AAPFordelerProd(private val arena : ArenaClient, arkiv : ArkivClient, manuell : ManuellFordelingFactory,
                      override val cfg : FordelerConfig = PROD_AAP) : AAPFordeler(arena, arkiv, manuell) {

    val log = getLogger(AAPFordelerProd::class.java)

    override fun opprettArenaOppgave(jp : Journalpost, enhet : NAVEnhet) = log.info("Dummy ferdigstilling av søknad med journalpostId ${jp.id}")

    override fun ferdigstillEttersending(jp : Journalpost, nyesteSak : String) =
        log.info("Dummy ferdigstilling av ettersending med journalpostId ${jp.id}")

    override fun toString() = "AAPFordelerProd($arena=$arena), cfg=$cfg, manuell=${manuell})"
}