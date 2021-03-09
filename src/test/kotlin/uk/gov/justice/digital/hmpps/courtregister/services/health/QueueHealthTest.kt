package uk.gov.justice.digital.hmpps.courtregister.services.health

import com.amazonaws.services.sqs.AmazonSQS
import com.amazonaws.services.sqs.model.GetQueueAttributesRequest
import com.amazonaws.services.sqs.model.GetQueueAttributesResult
import com.amazonaws.services.sqs.model.GetQueueUrlResult
import com.amazonaws.services.sqs.model.QueueAttributeName
import com.amazonaws.services.sqs.model.QueueDoesNotExistException
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.springframework.boot.actuate.health.Status
import uk.gov.justice.digital.hmpps.courtregister.services.health.QueueAttributes.MESSAGES_IN_FLIGHT
import uk.gov.justice.digital.hmpps.courtregister.services.health.QueueAttributes.MESSAGES_ON_QUEUE

class QueueHealthTest {

  private val someQueueName = "some queue name"
  private val someQueueUrl = "some queue url"
  private val someMessagesOnQueueCount = 123
  private val someMessagesInFlightCount = 456
  private val amazonSqs: AmazonSQS = mock()
  private val queueHealth: QueueHealth = HmppsAuditQueueHealth(amazonSqs, someQueueName)

  @Test
  fun `health - queue found - UP`() {
    mockHealthyQueue()

    val health = queueHealth.health()

    assertThat(health.status).isEqualTo(Status.UP)
  }

  @Test
  fun `health - attributes returned - included in health status`() {
    mockHealthyQueue()

    val health = queueHealth.health()

    assertThat(health.details[MESSAGES_ON_QUEUE.healthName]).isEqualTo(someMessagesOnQueueCount)
    assertThat(health.details[MESSAGES_IN_FLIGHT.healthName]).isEqualTo(someMessagesInFlightCount)
  }

  @Test
  fun `health - queue not found - DOWN`() {
    whenever(amazonSqs.getQueueUrl(anyString())).thenThrow(QueueDoesNotExistException::class.java)

    val health = queueHealth.health()

    assertThat(health.status).isEqualTo(Status.DOWN)
  }

  @Test
  fun `health - failed to get main queue attributes - DOWN`() {
    whenever(amazonSqs.getQueueUrl(anyString())).thenReturn(someGetQueueUrlResult())
    whenever(amazonSqs.getQueueAttributes(someGetQueueAttributesRequest())).thenThrow(RuntimeException::class.java)

    val health = queueHealth.health()

    assertThat(health.status).isEqualTo(Status.DOWN)
  }

  private fun mockHealthyQueue() {
    whenever(amazonSqs.getQueueUrl(someQueueName)).thenReturn(someGetQueueUrlResult())
    whenever(amazonSqs.getQueueAttributes(someGetQueueAttributesRequest())).thenReturn(
      someGetQueueAttributesResultWithDLQ()
    )
  }

  private fun someGetQueueAttributesRequest() =
    GetQueueAttributesRequest(someQueueUrl).withAttributeNames(listOf(QueueAttributeName.All.toString()))

  private fun someGetQueueUrlResult(): GetQueueUrlResult = GetQueueUrlResult().withQueueUrl(someQueueUrl)

  private fun someGetQueueAttributesResultWithDLQ() = GetQueueAttributesResult().withAttributes(
    mapOf(
      MESSAGES_ON_QUEUE.awsName to someMessagesOnQueueCount.toString(),
      MESSAGES_IN_FLIGHT.awsName to someMessagesInFlightCount.toString(),
      QueueAttributeName.RedrivePolicy.toString() to "any redrive policy"
    )
  )
}
