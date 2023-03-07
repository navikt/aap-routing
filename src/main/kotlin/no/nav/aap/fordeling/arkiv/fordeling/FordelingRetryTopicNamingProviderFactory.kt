package no.nav.aap.fordeling.arkiv.fordeling

import org.springframework.kafka.retrytopic.DestinationTopic.Properties
import org.springframework.kafka.retrytopic.RetryTopicNamesProviderFactory
import org.springframework.kafka.retrytopic.RetryTopicNamesProviderFactory.RetryTopicNamesProvider
import org.springframework.kafka.retrytopic.SuffixingRetryTopicNamesProviderFactory.SuffixingRetryTopicNamesProvider
import org.springframework.stereotype.Component

@Component
class FordelingRetryTopicNamingProviderFactory(private val cf: FordelingConfig) : RetryTopicNamesProviderFactory {

    override fun createRetryTopicNamesProvider(p: Properties): RetryTopicNamesProvider {
        with(cf.topics) {
            if (p.isDltTopic) {
                return object : SuffixingRetryTopicNamesProvider(p) {
                    override fun getTopicName(topic: String) = dlt
                }
            }
            if (p.isMainEndpoint) {
                return object : SuffixingRetryTopicNamesProvider(p) {
                    override fun getTopicName(topic: String) = topic
                }
            }
            return object : SuffixingRetryTopicNamesProvider(p) { // retry
                override fun getTopicName(topic: String) = retry
            }
        }
    }
}