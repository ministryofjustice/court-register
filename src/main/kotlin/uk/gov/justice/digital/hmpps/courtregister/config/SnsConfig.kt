package uk.gov.justice.digital.hmpps.courtregister.config

import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.sns.AmazonSNSAsync
import com.amazonaws.services.sns.AmazonSNSAsyncClientBuilder
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary

@Configuration
class SnsConfig {

  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  @Bean
  @ConditionalOnProperty(name = ["sns.provider"], havingValue = "aws")
  @Primary
  fun awsSnsClient(
    @Value("\${sns.aws.access.key.id}") accessKey: String?,
    @Value("\${sns.aws.secret.access.key}") secretKey: String?,
    @Value("\${sns.endpoint.region}") region: String?
  ): AmazonSNSAsync? {
    log.debug("Initialising AWS SNS Client")
    val creds = BasicAWSCredentials(accessKey, secretKey)
    return AmazonSNSAsyncClientBuilder.standard()
      .withCredentials(AWSStaticCredentialsProvider(creds))
      .withRegion(region)
      .build()
  }
}
