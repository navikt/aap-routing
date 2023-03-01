package no.nav.aap.fordeling.arkiv

import no.nav.aap.util.LoggerUtil.getLogger
import org.springframework.kafka.retrytopic.DestinationTopic.Properties
import org.springframework.kafka.retrytopic.RetryTopicComponentFactory
import org.springframework.kafka.retrytopic.RetryTopicConfigurationSupport
import org.springframework.kafka.retrytopic.RetryTopicNamesProviderFactory
import org.springframework.kafka.retrytopic.RetryTopicNamesProviderFactory.RetryTopicNamesProvider
import org.springframework.kafka.retrytopic.SuffixingRetryTopicNamesProviderFactory.SuffixingRetryTopicNamesProvider
import org.springframework.stereotype.Component

private  class CustomTopicNamingProviderFactory : RetryTopicNamesProviderFactory {

    val log = getLogger(javaClass)

    override fun createRetryTopicNamesProvider(p: Properties): RetryTopicNamesProvider {
             log.info("XXXXXXXXXXXXXXXXXX")
             if (p.isMainEndpoint) {
                 return object : SuffixingRetryTopicNamesProvider(p) {
                     override fun getTopicName(topic: String) = "aap.routing.main"
                 }
             }
             if (p.isDltTopic) {
                 return object : SuffixingRetryTopicNamesProvider(p) {
                     override fun getTopicName(topic: String) = "aap.routing.dlt"

                 }
             }
             return object : SuffixingRetryTopicNamesProvider(p) {
                 override fun getTopicName(topic: String) = "aap.routing.retry"
             }
         }
 }

@Component
class CustomRetryTopicConfigurationSupport : RetryTopicConfigurationSupport() {
    override fun createComponentFactory(): RetryTopicComponentFactory = object : RetryTopicComponentFactory() {
        override fun retryTopicNamesProviderFactory(): RetryTopicNamesProviderFactory =
            CustomTopicNamingProviderFactory()
    }
}