package no.nav.aap.fordeling.arkiv

import no.nav.aap.util.LoggerUtil.getLogger
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.kafka.retrytopic.DestinationTopic.Properties
import org.springframework.kafka.retrytopic.RetryTopicComponentFactory
import org.springframework.kafka.retrytopic.RetryTopicConfigurationSupport
import org.springframework.kafka.retrytopic.RetryTopicNamesProviderFactory
import org.springframework.kafka.retrytopic.RetryTopicNamesProviderFactory.RetryTopicNamesProvider
import org.springframework.kafka.retrytopic.SuffixingRetryTopicNamesProviderFactory.SuffixingRetryTopicNamesProvider
import org.springframework.stereotype.Component

  class CustomTopicNamingProviderFactory : RetryTopicNamesProviderFactory {

    val log = getLogger(javaClass)

    override fun createRetryTopicNamesProvider(p: Properties): RetryTopicNamesProvider {
        if (p.isDltTopic) {
            return object : SuffixingRetryTopicNamesProvider(p) {
                override fun getTopicName(topic: String) = "aap.routing.dlt"
            }
        }
        if (!p.isDltTopic && !p.isMainEndpoint) {
            return object : SuffixingRetryTopicNamesProvider(p) {
                override fun getTopicName(topic: String) = "aap.routing.retry"
            }
        }
        return object : SuffixingRetryTopicNamesProvider(p) {
            override fun getTopicName(topic: String) = "teamdokumenthandtering.aapen-dok-journalfoering"
        }
    }


}