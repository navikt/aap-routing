package no.nav.aap.fordeling.util

import io.micrometer.context.ContextRegistry
import io.micrometer.context.ThreadLocalAccessor
import org.slf4j.MDC
import reactor.core.publisher.Hooks

class MDCAccessor : ThreadLocalAccessor<Map<String, String>> {

    override fun key() = KEY

    override fun getValue() = MDC.getCopyOfContextMap() ?: emptyMap()

    override fun setValue(map : Map<String, String>) = MDC.setContextMap(map)

    override fun reset() = MDC.clear()

    companion object {

        private const val KEY = "mdc"
    }
}

object MDCAccessorUtil {

    fun init() = run {
        Hooks.enableAutomaticContextPropagation()
        ContextRegistry.getInstance().registerThreadLocalAccessor(MDCAccessor())
    }
}