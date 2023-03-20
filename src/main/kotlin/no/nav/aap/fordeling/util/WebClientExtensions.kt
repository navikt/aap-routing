package no.nav.aap.fordeling.util

import no.nav.aap.api.felles.error.IrrecoverableIntegrationException
import no.nav.aap.api.felles.error.RecoverableIntegrationException
import org.slf4j.Logger
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.kotlin.core.publisher.toMono

object WebClientExtensions {

    fun ClientResponse.toResponse(log: Logger) =
        with(statusCode()){
            if (is2xxSuccessful)
                bodyToMono<Boolean>()
            else if (is4xxClientError)
                bodyToMono<String>().flatMap {
                    log.warn(it)
                    IrrecoverableIntegrationException(it).toMono()
                }
            else
                bodyToMono<String>().flatMap {
                    RecoverableIntegrationException(it).toMono()
                }
        }
}