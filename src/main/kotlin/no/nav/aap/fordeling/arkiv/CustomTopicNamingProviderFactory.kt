package no.nav.aap.fordeling.arkiv

import no.nav.aap.util.LoggerUtil.getLogger
import org.springframework.kafka.retrytopic.DestinationTopic.Properties
import org.springframework.kafka.retrytopic.RetryTopicComponentFactory
import org.springframework.kafka.retrytopic.RetryTopicConfigurationSupport
import org.springframework.kafka.retrytopic.RetryTopicNamesProviderFactory
import org.springframework.kafka.retrytopic.RetryTopicNamesProviderFactory.RetryTopicNamesProvider
import org.springframework.kafka.retrytopic.SuffixingRetryTopicNamesProviderFactory.SuffixingRetryTopicNamesProvider

private  class CustomTopicNamingProviderFactory : RetryTopicNamesProviderFactory {

    val log = getLogger(javaClass)

    override fun createRetryTopicNamesProvider(p: Properties): RetryTopicNamesProvider {
        log.info("XXXXXXXXXXXXXXXXXX")
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
            override fun getTopicName(topic: String) = "aap.routing.main"
        }
    }

    //@Component
    //@Primary
    class CustomRetryTopicConfigurationSupport : RetryTopicConfigurationSupport() {
        override fun createComponentFactory() = object : RetryTopicComponentFactory() {
            override fun retryTopicNamesProviderFactory() =
                CustomTopicNamingProviderFactory()
        }
    }
}