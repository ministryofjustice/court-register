package uk.gov.justice.digital.hmpps.courtregister.service

import com.amazonaws.services.sns.AmazonSNSAsync
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.cloud.aws.messaging.core.NotificationMessagingTemplate
import org.springframework.cloud.aws.messaging.core.TopicMessageChannel
import org.springframework.stereotype.Service
import uk.gov.justice.hmpps.sqs.HmppsQueueService

@Service
class SnsService(
  hmppsQueueService: HmppsQueueService,
) {
  private val hmppsTopic by lazy {
    hmppsQueueService.findByTopicId("domainevents")
      ?: throw RuntimeException("Topic with name domainevents doesn't exist")
  }
  private val topicArn by lazy { hmppsTopic.arn }
  private val awsSnsClient by lazy { hmppsTopic.snsClient as AmazonSNSAsync }

  private val topicTemplate: NotificationMessagingTemplate = NotificationMessagingTemplate(awsSnsClient)

  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
    val gson: Gson = GsonBuilder().create()
  }

  fun sendEvent(eventType: EventType, id: String) {
    log.debug("Event {} for id {}", eventType, id)
    topicTemplate.convertAndSend(
      TopicMessageChannel(awsSnsClient, topicArn),
      gson.toJson(RegisterChangeEvent(eventType, id)),
      mapOf("eventType" to eventType.name),
    )
  }
}

data class RegisterChangeEvent(
  val eventType: EventType,
  val id: String,
)

enum class AuditType {
  COURT_REGISTER_INSERT, COURT_REGISTER_UPDATE,
  COURT_REGISTER_BUILDING_INSERT, COURT_REGISTER_BUILDING_UPDATE, COURT_REGISTER_BUILDING_DELETE,
  COURT_REGISTER_CONTACT_INSERT, COURT_REGISTER_CONTACT_UPDATE, COURT_REGISTER_CONTACT_DELETE
}

enum class EventType {
  COURT_REGISTER_UPDATE,
}
