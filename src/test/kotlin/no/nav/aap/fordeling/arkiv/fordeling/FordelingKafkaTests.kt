package no.nav.aap.fordeling.arkiv.fordeling

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.kafka.test.EmbeddedKafkaBroker
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import no.nav.aap.fordeling.arkiv.ArkivClient

@EmbeddedKafka
@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes = [ArkivClient::class])
public class FordelingKafkaTests {

    @Autowired
    lateinit var broker : EmbeddedKafkaBroker

    @MockBean
    lateinit var arkiv : ArkivClient

    @Test
    fun test() {
        val brokerList = broker.brokersAsString;
        println("XXXX  " + arkiv)
    }
}