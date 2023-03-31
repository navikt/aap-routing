import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.kafka.test.EmbeddedKafkaBroker
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import no.nav.aap.fordeling.arkiv.fordeling.FordelingBeslutter
import no.nav.aap.fordeling.arkiv.fordeling.FordelingConfig

@EmbeddedKafka
@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes = [FordelingBeslutter::class, FordelingConfig::class])
//@SpringJUnitConfig(classes = [MottattAwareFordelingBeslutter::class])
public class KafkaTests {

    @Autowired
    lateinit var broker : EmbeddedKafkaBroker

    @Test
    fun test() {
        val brokerList = broker.brokersAsString;
        println("XXXX  " + broker)
    }
}