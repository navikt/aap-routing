package no.nav.aap.fordeling.config

import io.micrometer.core.instrument.config.MeterFilter.*
import io.netty.handler.logging.LogLevel.*
import java.util.*
import java.util.concurrent.TimeUnit.*
import kotlin.random.Random.Default.nextInt
import no.nav.aap.util.ChaosMonkey
import no.nav.aap.util.LoggerUtil.getLogger
import no.nav.boot.conditionals.Cluster
import no.nav.boot.conditionals.Cluster.*
import no.nav.boot.conditionals.Cluster.Companion.currentCluster
import no.nav.boot.conditionals.ConditionalOnNotProd
import no.nav.boot.conditionals.ConditionalOnProd
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders.*
import org.springframework.http.HttpStatus.*
import org.springframework.web.reactive.function.client.ExchangeFilterFunction.*

@Configuration
class ChaosMonkeyConfig {

    private val log = getLogger(ChaosMonkeyConfig::class.java)

    @Bean
    @ConditionalOnNotProd
    fun notProdChaosMonkey() = ChaosMonkey(DEV_MONKEY)

    @Bean
    @ConditionalOnProd
    fun prodChaosMonkey() = ChaosMonkey(PROD_MONKEY)

    @Bean
    @ConditionalOnNotProd
    @Qualifier(MONKEY)
    fun notProdFilterMonkey(monkey: ChaosMonkey) = monkey.chaosMonkeyRequestFilterFunction(DEV_FILTER_MONKEY)

    @Bean
    @ConditionalOnProd
    @Qualifier(MONKEY)
    fun prodFilterMonkey(monkey: ChaosMonkey) = monkey.chaosMonkeyRequestFilterFunction(PROD_FILTER_MONKEY)


    companion object {

        const val MONKEY = "chaos-monkey"

        val NO_MONKEY = { false }

        val DEV_MONKEY = NO_MONKEY // monkey(DEV_GCP)

        val DEV_FILTER_MONKEY =  NO_MONKEY //monkey(DEV_GCP)

        val PROD_MONKEY = NO_MONKEY

        val PROD_FILTER_MONKEY =  NO_MONKEY //monkey(PROD_GCP)

        val LOCAL_MONKEY =   monkeyIn(arrayOf(LOCAL))

         fun monkeyIn(clusters: Array<Cluster>, n: Int = 5) = { -> nextInt(1, n) == 1 && currentCluster in clusters.asList() }
    }
}