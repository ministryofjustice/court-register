package uk.gov.justice.digital.hmpps.courtregister.resource

import com.amazonaws.services.sqs.AmazonSQSAsync
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient

@Suppress("SpringJavaInjectionPointsAutowiringInspection")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
abstract class IntegrationTest {
  @Suppress("unused")
  @Autowired
  lateinit var webTestClient: WebTestClient

  @Autowired
  protected lateinit var awsSqsClient: AmazonSQSAsync

  @Value("\${sqs.queue.name}")
  protected lateinit var queueName: String

  init {
    // Resolves an issue where Wiremock keeps previous sockets open from other tests causing connection resets
    System.setProperty("http.keepAlive", "false")
  }

  fun String.queueUrl(): String = awsSqsClient.getQueueUrl(this).queueUrl
}
