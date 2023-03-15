package no.nav.aap.fordeling.config

import io.github.resilience4j.retry.RetryRegistry
import no.nav.aap.fordeling.graphql.AbstractGraphQLAdapter.Companion.GRAPHQL
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class Resilience4jRetryListener(registry: RetryRegistry) {
    private val log = LoggerFactory.getLogger(Resilience4jRetryListener::class.java)
    init {
        registry.retry(GRAPHQL).eventPublisher.onRetry {
            event -> log.info("Retry event $event")
        }
    }
}