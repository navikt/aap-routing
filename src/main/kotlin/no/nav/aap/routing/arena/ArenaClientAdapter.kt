package no.nav.aap.routing.arena

import no.nav.aap.api.felles.error.IntegrationException
import no.nav.aap.rest.AbstractWebClientAdapter
import no.nav.aap.routing.arena.ArenaConfig.Companion.ARENA
import no.nav.aap.routing.navorganisasjon.EnhetsKriteria
import no.nav.aap.routing.navorganisasjon.NavOrg
import no.nav.aap.routing.navorganisasjon.NavOrgConfig
import no.nav.aap.routing.navorganisasjon.NavOrgConfig.Companion.AKTIV
import no.nav.aap.routing.navorganisasjon.NavOrgConfig.Companion.ENHETSLISTE
import no.nav.aap.routing.navorganisasjon.NavOrgConfig.Companion.NAVORG
import no.nav.aap.routing.navorganisasjon.NavOrgWebClientAdapter
import no.nav.aap.routing.person.Diskresjonskode
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpStatus.*
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

@Component
class ArenaWebClientAdapter(@Qualifier(ARENA) webClient: WebClient, val cf: ArenaConfig) :
    AbstractWebClientAdapter(webClient, cf) {
}

@Component
class ArenaClient(private val adapter: ArenaWebClientAdapter) {
}