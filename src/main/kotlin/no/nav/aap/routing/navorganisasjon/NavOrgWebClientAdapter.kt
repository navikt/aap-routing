package no.nav.aap.routing.navorganisasjon

import no.nav.aap.rest.AbstractWebClientAdapter
import no.nav.aap.routing.navorganisasjon.NavOrgConfig.Companion.ORG
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpStatus.*
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient

@Component
class NavOrgWebClientAdapter(@Qualifier(ORG) webClient: WebClient, val cf: NavOrgConfig) :
    AbstractWebClientAdapter(webClient, cf) {


}