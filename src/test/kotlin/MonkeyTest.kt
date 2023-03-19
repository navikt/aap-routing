import no.nav.aap.fordeling.config.GlobalBeanConfig.Companion.LOCAL_MONKEY
import org.junit.jupiter.api.Test

class MonkeyTest {

    @Test
    fun monkey() {
        for (i in 1..100)
        println(LOCAL_MONKEY.invoke())
    }
}