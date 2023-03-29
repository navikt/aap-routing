package no.nav.aap.fordeling.graphql

import io.github.resilience4j.retry.RetryRegistry
import io.github.resilience4j.springboot3.retry.autoconfigure.RetryProperties
import jakarta.annotation.PostConstruct
import no.nav.aap.fordeling.arkiv.saf.SAFConfig.Companion.SAF
import no.nav.aap.fordeling.person.PDLConfig.Companion.PDL
import no.nav.aap.fordeling.slack.Slacker
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class GraphQLRetryListener(private val registry: RetryRegistry, private val slacker: Slacker, private val p: RetryProperties) {
    private val log = LoggerFactory.getLogger(GraphQLRetryListener::class.java)

    @PostConstruct
    fun init() = p.instances.keys.forEach { registerListener(it) }

    private fun registerListener(key: String) {
        log.info("Registrerer retry listener for ${key.uppercase()}")
        with(registry.retry(key)) {
            eventPublisher.onError {
                with("Retry event error $it") {
                    log.warn(this, it.lastThrowable)
                    slacker.feilHvisDev(this)
                }
            }
            eventPublisher.onSuccess {
                with("Retry event success ${key.uppercase()} $it") {
                    log.info(this)
                    slacker.okHvisDev(this)
                }
            }
            eventPublisher.onIgnoredError() {
                with("Retry event ignore ${key.uppercase()} $it") {
                    log.warn(this, it.lastThrowable)
                    slacker.feilHvisDev(this)
                }
            }
            eventPublisher.onRetry {
                with("Retry event ${key.uppercase()} $it") {
                    log.warn(this, it.lastThrowable)
                    slacker.feilHvisDev(this)
                }
            }
        }
    }
}