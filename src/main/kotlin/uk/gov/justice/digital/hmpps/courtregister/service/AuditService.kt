package uk.gov.justice.digital.hmpps.courtregister.service

import com.amazonaws.services.sqs.AmazonSQSAsync
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.cloud.aws.messaging.core.QueueMessagingTemplate
import org.springframework.messaging.support.MessageBuilder
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.courtregister.config.SecurityUserContext
import uk.gov.justice.hmpps.sqs.HmppsQueueService
import java.time.Instant

@Service
class AuditService(
  hmppsQueueService: HmppsQueueService,
  @Value("\${spring.application.name}")
  private val serviceName: String,
  private val securityUserContext: SecurityUserContext,
  private val mapper: ObjectMapper
) {
  private val hmppsQueue by lazy {
    hmppsQueueService.findByQueueId("audit") ?: throw RuntimeException("Queue with name audit doesn't exist")
  }
  private val queueName by lazy { hmppsQueue.queueName }
  private val awsSqsClient by lazy { hmppsQueue.sqsClient as AmazonSQSAsync }

  private val auditMessagingTemplate: QueueMessagingTemplate = QueueMessagingTemplate(awsSqsClient)

  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  fun sendAuditEvent(what: String, details: Any) {
    val auditEvent = AuditEvent(
      what = what,
      who = securityUserContext.principal,
      service = serviceName,
      details = mapper.writeValueAsString(details)
    )
    log.debug("Audit {} ", auditEvent)
    auditMessagingTemplate.send(
      queueName,
      MessageBuilder.withPayload(mapper.writeValueAsString(auditEvent)).build()
    )
  }
}

data class AuditEvent(
  val what: String,
  val `when`: Instant = Instant.now(),
  val who: String,
  val service: String,
  val details: String? = null,
)
