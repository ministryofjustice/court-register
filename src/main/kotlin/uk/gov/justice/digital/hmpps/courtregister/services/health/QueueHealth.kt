package uk.gov.justice.digital.hmpps.courtregister.services.health

import com.amazonaws.services.sqs.AmazonSQS
import com.amazonaws.services.sqs.model.GetQueueAttributesRequest
import com.amazonaws.services.sqs.model.GetQueueUrlResult
import com.amazonaws.services.sqs.model.QueueAttributeName.All
import com.amazonaws.services.sqs.model.QueueAttributeName.ApproximateNumberOfMessages
import com.amazonaws.services.sqs.model.QueueAttributeName.ApproximateNumberOfMessagesNotVisible
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.Health.Builder
import org.springframework.boot.actuate.health.HealthIndicator
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.courtregister.services.health.QueueAttributes.MESSAGES_IN_FLIGHT
import uk.gov.justice.digital.hmpps.courtregister.services.health.QueueAttributes.MESSAGES_ON_QUEUE

enum class QueueAttributes(val awsName: String, val healthName: String) {
  MESSAGES_ON_QUEUE(ApproximateNumberOfMessages.toString(), "MessagesOnQueue"),
  MESSAGES_IN_FLIGHT(ApproximateNumberOfMessagesNotVisible.toString(), "MessagesInFlight"),
}

abstract class QueueHealth(
  private val awsSqsClient: AmazonSQS,
  private val queueName: String,
) : HealthIndicator {

  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  override fun health(): Health {
    val queueAttributes = try {
      val url = awsSqsClient.getQueueUrl(queueName)
      awsSqsClient.getQueueAttributes(getQueueAttributesRequest(url))
    } catch (e: Exception) {
      log.error("Unable to retrieve queue attributes for queue '{}' due to exception:", queueName, e)
      return Builder().down().withException(e).build()
    }
    val details = mutableMapOf<String, Any?>(
      MESSAGES_ON_QUEUE.healthName to queueAttributes.attributes[MESSAGES_ON_QUEUE.awsName]?.toInt(),
      MESSAGES_IN_FLIGHT.healthName to queueAttributes.attributes[MESSAGES_IN_FLIGHT.awsName]?.toInt()
    )

    return Builder().up().withDetails(details).build()
  }


  private fun getQueueAttributesRequest(url: GetQueueUrlResult) =
    GetQueueAttributesRequest(url.queueUrl).withAttributeNames(All)
}

@Component
class HmppsAuditQueueHealth
constructor(
  @Qualifier("awsSqsClient") awsSqsClient: AmazonSQS,
  @Value("\${sqs.queue.name}") private val queueName: String,
) : QueueHealth(awsSqsClient, queueName)
