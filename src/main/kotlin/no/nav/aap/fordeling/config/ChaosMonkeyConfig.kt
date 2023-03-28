package no.nav.aap.fordeling.config


import kotlin.random.Random.Default.nextInt

import no.nav.boot.conditionals.Cluster
import no.nav.boot.conditionals.Cluster.*
import no.nav.boot.conditionals.Cluster.Companion.currentCluster
import no.nav.boot.conditionals.Cluster.Companion.devClusters


object  ChaosMonkeyConfig {
        const val MONKEY = "chaos-monkey"

        val NO_MONKEY = { false }

        val DEV_MONKEY = NO_MONKEY // monkey(DEV_GCP)

        val DEV_FILTER_MONKEY =  NO_MONKEY //monkey(DEV_GCP)

        val PROD_MONKEY = NO_MONKEY

        val PROD_FILTER_MONKEY =  NO_MONKEY //monkey(PROD_GCP)

        val LOCAL_MONKEY =   monkeyIn(arrayOf(LOCAL))

         fun monkeyIn(clusters: Array<Cluster> = devClusters(), n: Int = 5) = { -> nextInt(1, n) == 1 && currentCluster in clusters.asList() }
}