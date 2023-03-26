import no.nav.aap.fordeling.arkiv.fordeling.FordelingConfig
import no.nav.aap.fordeling.arkiv.fordeling.MottattAwareFordelingBeslutter
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.kafka.test.EmbeddedKafkaBroker
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension

@EmbeddedKafka
@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes = [MottattAwareFordelingBeslutter::class, FordelingConfig::class])
//@SpringJUnitConfig(classes = [MottattAwareFordelingBeslutter::class])
public class KafkaTests {

    @Autowired
    lateinit var broker: EmbeddedKafkaBroker

    @Test
    fun test() {
        val brokerList = broker.brokersAsString;
        println("XXXX  " + broker)
    }

}