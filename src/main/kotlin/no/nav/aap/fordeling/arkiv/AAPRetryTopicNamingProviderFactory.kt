package no.nav.aap.fordeling.arkiv

import no.nav.aap.util.LoggerUtil.getLogger
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.kafka.retrytopic.DestinationTopic.Properties
import org.springframework.kafka.retrytopic.RetryTopicNamesProviderFactory
import org.springframework.kafka.retrytopic.RetryTopicNamesProviderFactory.RetryTopicNamesProvider
import org.springframework.kafka.retrytopic.SuffixingRetryTopicNamesProviderFactory.SuffixingRetryTopicNamesProvider
import org.springframework.stereotype.Component

@Component
class AAPRetryTopicNamingProviderFactory(private val cf: RoutingConfig) : RetryTopicNamesProviderFactory {

    val log = getLogger(javaClass)

      override fun createRetryTopicNamesProvider(p: Properties): RetryTopicNamesProvider {
          if (p.isDltTopic) {
              return object : SuffixingRetryTopicNamesProvider(p) {
                  override fun getTopicName(topic: String) = cf.dlt
              }
          }
          if (p.isMainEndpoint) {
              return object : SuffixingRetryTopicNamesProvider(p) {
                  override fun getTopicName(topic: String) = topic
              }
          }
          return object : SuffixingRetryTopicNamesProvider(p) { // retry
              override fun getTopicName(topic: String) = cf.retry
          }
      }
}