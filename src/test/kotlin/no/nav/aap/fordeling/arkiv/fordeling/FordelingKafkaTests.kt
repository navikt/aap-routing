package no.nav.aap.fordeling.arkiv.fordeling

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.kafka.test.EmbeddedKafkaBroker
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension

@EmbeddedKafka
@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes = [FordelingBeslutter::class, FordelingConfig::class])
public class FordelingKafkaTests {

    @Autowired
    lateinit var broker : EmbeddedKafkaBroker

    @Test
    fun test() {
        val brokerList = broker.brokersAsString;
        println("XXXX  " + broker)
    }
}