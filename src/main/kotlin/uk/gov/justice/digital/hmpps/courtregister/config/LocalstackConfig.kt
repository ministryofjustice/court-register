package uk.gov.justice.digital.hmpps.courtregister.config

import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.AnonymousAWSCredentials
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration
import com.amazonaws.services.sns.AmazonSNSAsync
import com.amazonaws.services.sns.AmazonSNSAsyncClientBuilder
import com.amazonaws.services.sqs.AmazonSQSAsync
import com.amazonaws.services.sqs.AmazonSQSAsyncClientBuilder
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary

@Configuration
class LocalstackConfig {

  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  @Bean
  @ConditionalOnProperty(name = ["sns.provider"], havingValue = "localstack")
  @Primary
  fun awsSnsClient(
    @Value("\${sns.endpoint.url}") serviceEndpoint: String?,
    @Value("\${sns.endpoint.region}") region: String?
  ): AmazonSNSAsync? {
    log.debug("Initialising Localstack SNS Client")

    return AmazonSNSAsyncClientBuilder.standard()
      .withEndpointConfiguration(EndpointConfiguration(serviceEndpoint, region))
      .build()
  }

  @Bean
  @ConditionalOnProperty(name = ["sqs.provider"], havingValue = "localstack")
  @Primary
  fun awsSqsClient(
    @Value("\${sqs.endpoint.url}") serviceEndpoint: String,
    @Value("\${sqs.endpoint.region}") region: String
  ): AmazonSQSAsync =
    AmazonSQSAsyncClientBuilder.standard()
      .withEndpointConfiguration(EndpointConfiguration(serviceEndpoint, region))
      .withCredentials(AWSStaticCredentialsProvider(AnonymousAWSCredentials()))
      .build()
}
