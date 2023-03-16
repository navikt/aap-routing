package no.nav.aap.fordeling.config

import io.github.resilience4j.retry.RetryRegistry
import no.nav.aap.fordeling.graphql.AbstractGraphQLAdapter.Companion.GRAPHQL
import no.nav.aap.fordeling.slack.Slacker
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class Resilience4jRetryListener(registry: RetryRegistry, slacker: Slacker) {
    private val log = LoggerFactory.getLogger(Resilience4jRetryListener::class.java)

    init {
        with(registry.retry(GRAPHQL)) {
            eventPublisher.onError {
                with("Retry event error $it") {
                    log.warn(this,it.lastThrowable)
                    slacker.feilHvisDev(this)
                }
            }
            eventPublisher.onSuccess {
                with("Retry event success $it") {
                    log.info(this)
                    slacker.okHvisDev(this)
                }
            }
            eventPublisher.onIgnoredError() {
                with("Retry event ignore $it") {
                    log.warn(this,it.lastThrowable)
                    slacker.feilHvisDev(this)
                }
            }
            registry.retry(GRAPHQL).eventPublisher.onRetry {
                with("Retry event $it") {
                    log.warn(this,it.lastThrowable)
                    slacker.feilHvisDev(this)
                }
            }
        }
    }
}