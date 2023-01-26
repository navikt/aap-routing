package no.nav.aap.routing

import no.nav.boot.conditionals.Cluster.Companion.profiler
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.kafka.annotation.EnableKafka

@SpringBootApplication
@EnableKafka
class RoutingApplication

fun main(args: Array<String>) {
	runApplication<RoutingApplication>(*args) {
		setAdditionalProfiles(*profiler())
	}
}