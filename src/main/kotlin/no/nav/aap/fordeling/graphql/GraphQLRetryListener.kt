package no.nav.aap.fordeling.graphql

import io.github.resilience4j.retry.RetryRegistry
import no.nav.aap.fordeling.arkiv.saf.SAFConfig.Companion.SAF
import no.nav.aap.fordeling.graphql.AbstractGraphQLAdapter.Companion.GRAPHQL
import no.nav.aap.fordeling.person.PDLConfig.Companion.PDL
import no.nav.aap.fordeling.slack.Slacker
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class GraphQLRetryListener(private val registry: RetryRegistry, private val slacker: Slacker) {
    private val log = LoggerFactory.getLogger(GraphQLRetryListener::class.java)

    init {
        register(SAF)
        register(PDL)
    }

    private fun GraphQLRetryListener.register(cfg: String) {
        log.info("Registrerer retry listener for $cfg")
        with(registry.retry(SAF)) {
            eventPublisher.onError {
                with("Retry event error $it") {
                    log.warn(this, it.lastThrowable)
                    slacker.feilHvisDev(this)
                }
            }
            eventPublisher.onSuccess {
                with("Retry event success ${cfg.uppercase()} $it") {
                    log.info(this)
                    slacker.okHvisDev(this)
                }
            }
            eventPublisher.onIgnoredError() {
                with("Retry event ignore ${cfg.uppercase()} $it") {
                    log.warn(this, it.lastThrowable)
                    slacker.feilHvisDev(this)
                }
            }
            registry.retry(GRAPHQL).eventPublisher.onRetry {
                with("Retry event ${cfg.uppercase()} $it") {
                    log.warn(this, it.lastThrowable)
                    slacker.feilHvisDev(this)
                }
            }
        }
    }
}