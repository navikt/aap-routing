package no.nav.aap.fordeling

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import no.nav.boot.conditionals.Cluster.Companion.profiler
import no.nav.security.token.support.client.spring.oauth2.EnableOAuth2Client
import no.nav.security.token.support.spring.api.EnableJwtTokenValidation

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableOAuth2Client(cacheEnabled = true)
@EnableJwtTokenValidation(ignore = ["org.springdoc", "org.springframework"])
class RoutingApplication

fun main(args : Array<String>) {
    runApplication<RoutingApplication>(*args) {
        setAdditionalProfiles(*profiler())
    }
}