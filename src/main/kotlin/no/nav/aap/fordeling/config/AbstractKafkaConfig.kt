package no.nav.aap.fordeling.config

abstract class AbstractKafkaConfig(val name: String, val isEnabled: Boolean) {
    abstract fun topics(): List<String>
}