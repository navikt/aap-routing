package no.nav.aap.fordeling

import io.micrometer.context.ContextRegistry
import io.micrometer.context.ThreadLocalAccessor
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.web.context.request.RequestAttributes
import org.springframework.web.context.request.RequestContextHolder
import reactor.core.publisher.Hooks.enableAutomaticContextPropagation
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
        enableAutomaticContextPropagation()
        ContextRegistry.getInstance().apply {
            registerThreadLocalAccessor(RequestAttributesAccessor())
        }
        setAdditionalProfiles(*profiler())
    }
}

class RequestAttributesAccessor : ThreadLocalAccessor<RequestAttributes> {

    override fun key() = RequestAttributesAccessor::class.java.name

    override fun getValue() = RequestContextHolder.getRequestAttributes()

    override fun setValue(value : RequestAttributes) = RequestContextHolder.setRequestAttributes(value)

    override fun reset() = RequestContextHolder.resetRequestAttributes()
}