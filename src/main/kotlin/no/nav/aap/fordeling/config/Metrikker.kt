package no.nav.aap.fordeling.config

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import org.springframework.stereotype.Component

@Component
class Metrikker(private val registry: MeterRegistry) {
    fun inc(navn: String, vararg tags: String) = Counter.builder(navn)
        .tags(*tags.map(String::lowercase).toTypedArray())
        .register(registry)
        .increment()

    companion object {
        const val TITTEL = "tittel"
        const val FORDELINGSTYPE = "type"
        const val KANAL = "kanal"
        const val BREVKODE = "brevkode"
    }
}