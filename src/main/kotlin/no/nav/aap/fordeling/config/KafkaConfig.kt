package no.nav.aap.fordeling.config

abstract class KafkaConfig(val name : String, val isEnabled : Boolean) {

    abstract fun topics() : List<String>
}