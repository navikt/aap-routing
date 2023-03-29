package no.nav.aap.fordeling.graphql

import io.github.resilience4j.retry.RetryRegistry
import io.github.resilience4j.springboot3.retry.autoconfigure.RetryProperties
import no.nav.aap.fordeling.slack.Slacker
import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component

@Component
class GraphQLRetryListener(private val registry: RetryRegistry, private val slacker: Slacker, private val p: RetryProperties) : ApplicationListener<ApplicationReadyEvent> {
    private val log = LoggerFactory.getLogger(GraphQLRetryListener::class.java)
    override fun onApplicationEvent(event: ApplicationReadyEvent)= p.instances.keys.forEach { registerRetryEventListeners(it) }
    private fun registerRetryEventListeners(key: String) {
        log.info("Registrerer retry listener for ${key.uppercase()}")
        with(registry.retry(key)) {
            with(eventPublisher) {
                onError {
                    with("Retry event error $it") {
                        log.warn(this, it.lastThrowable)
                        slacker.feilHvisDev(this)
                    }
                }
                onSuccess {
                    with("Vellykket retry på forsøk ${it.numberOfRetryAttempts} for '${it.name.uppercase()}' etter exception ${it.lastThrowable}") {
                        log.info(this)
                        slacker.okHvisDev(this)
                    }
                }
                onIgnoredError() {
                    with("Ingen retry for ${it.lastThrowable.javaClass.name}  for '${it.name.uppercase()}'") {
                        log.warn(this, it.lastThrowable)
                        slacker.feilHvisDev(this)
                    }
                }
                onRetry {
                    with("Gjør retry for ${it.numberOfRetryAttempts}. gang pga. ${it.lastThrowable.javaClass.name} for '${it.name.uppercase()}'") {
                        log.warn(this, it.lastThrowable)
                        slacker.feilHvisDev(this)
                    }
                }
            }
        }
    }
}