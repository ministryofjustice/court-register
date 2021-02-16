package uk.gov.justice.digital.hmpps.courtregister.service

import com.amazonaws.services.sns.AmazonSNSAsync
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.cloud.aws.messaging.core.NotificationMessagingTemplate
import org.springframework.cloud.aws.messaging.core.TopicMessageChannel
import org.springframework.stereotype.Service

@Service
class SnsService(
  private val awsSnsClient: AmazonSNSAsync,
  @Value("\${sns.topic.arn}") private val topicArn: String,

) {
  private val topicTemplate: NotificationMessagingTemplate = NotificationMessagingTemplate(awsSnsClient)

  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
    val gson: Gson = GsonBuilder().create()
  }

  fun sendEvent(eventType: EventType, id: String) {
    log.debug("Event {} for id {}", eventType, eventType, id)
    topicTemplate.convertAndSend(
      TopicMessageChannel(awsSnsClient, topicArn),
      gson.toJson(RegisterChangeEvent(eventType, id))
    )
  }
}

data class RegisterChangeEvent(
  val eventType: EventType,
  val id: String
)

enum class EventType {
  COURT_REGISTER_INSERT, COURT_REGISTER_UPDATE
}
