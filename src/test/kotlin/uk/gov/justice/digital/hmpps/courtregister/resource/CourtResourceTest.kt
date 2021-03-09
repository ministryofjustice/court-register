package uk.gov.justice.digital.hmpps.courtregister.resource

import com.amazonaws.services.sqs.AmazonSQSAsync
import com.amazonaws.services.sqs.model.PurgeQueueRequest
import com.nhaarman.mockito_kotlin.whenever
import net.javacrumbs.jsonunit.assertj.JsonAssertions
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.within
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.BodyInserters
import uk.gov.justice.digital.hmpps.courtregister.helper.JwtAuthHelper
import uk.gov.justice.digital.hmpps.courtregister.jpa.Court
import uk.gov.justice.digital.hmpps.courtregister.jpa.Court.CourtType.CROWN
import uk.gov.justice.digital.hmpps.courtregister.jpa.Court.CourtType.YOUTH
import uk.gov.justice.digital.hmpps.courtregister.jpa.CourtRepository
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.Optional

class CourtResourceTest : IntegrationTest() {
  @MockBean
  private lateinit var courtRepository: CourtRepository

  @Autowired
  protected lateinit var jwtAuthHelper: JwtAuthHelper

  @Autowired
  protected lateinit var awsSqsClient: AmazonSQSAsync

  @Value("\${sqs.queue.name}")
  protected lateinit var queueName: String


  @Suppress("ClassName")
  @Nested
  inner class findAll {
    @Test
    fun `find active courts`() {
      val courts = listOf(
        Court("ACCRYC", "Accrington Youth Court", null, YOUTH, true),
        Court("KIDDYC", "Kidderminster Youth Court", null, YOUTH, true)
      )

      whenever(courtRepository.findByActiveOrderById(true)).thenReturn(
        courts
      )
      webTestClient.get().uri("/courts")
        .exchange()
        .expectStatus().isOk
        .expectBody().json("courts".loadJson())
    }

    @Test
    fun `find all courts`() {
      val courts = listOf(
        Court("ACCRYC", "Accrington Youth Court", null, YOUTH, true),
        Court("KIDDYC", "Kidderminster Youth Court", null, YOUTH, true),
        Court("KIDDYE", "Kidderminster Crown Court", null, CROWN, false)
      )

      whenever(courtRepository.findAll()).thenReturn(
        courts
      )
      webTestClient.get().uri("/courts/all")
        .exchange()
        .expectStatus().isOk
        .expectBody().json("courts_all".loadJson())
    }

    @Test
    fun `find courts types`() {
      webTestClient.get().uri("/courts/types")
        .exchange()
        .expectStatus().isOk
        .expectBody().json("court_types".loadJson())
    }
  }

  @Suppress("ClassName")
  @Nested
  inner class updateAndInsertCourts {

    @BeforeEach
    internal fun drainAuditQueue() {
      awsSqsClient.purgeQueue(PurgeQueueRequest(queueName.queueUrl()))
    }

    @Test
    fun `correct permission are needed to update court data`() {
      webTestClient.put()
        .uri("/court-maintenance/id/ACCRYC")
        .accept(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation(roles = listOf("ROLE_DUMMY"), scopes = listOf("write")))
        .body(BodyInserters.fromValue(UpdateCourtDto("Updated Court", "a description", YOUTH, false)))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `correct scopes are needed to update court data`() {
      webTestClient.put()
        .uri("/court-maintenance/id/ACCRYC")
        .accept(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation(roles = listOf("ROLE_MAINTAIN_REF_DATA"), scopes = listOf("read")))
        .body(BodyInserters.fromValue(UpdateCourtDto("Updated Court", "a description", YOUTH, false)))
        .exchange().expectStatus().isForbidden
    }

    @Test
    fun `update a court`() {
      whenever(courtRepository.findById("ACCRYC")).thenReturn(
        Optional.of(Court("ACCRYC", "A Court 1", null, CROWN, true))
      )

      webTestClient.put()
        .uri("/court-maintenance/id/ACCRYC")
        .accept(MediaType.APPLICATION_JSON)
        .headers(
          setAuthorisation(
            roles = listOf("ROLE_MAINTAIN_REF_DATA"),
            scopes = listOf("write"),
            user = "bobby.beans"
          )
        )
        .body(BodyInserters.fromValue(UpdateCourtDto("Updated Court", "a description", YOUTH, false)))
        .exchange()
        .expectStatus().isOk
        .expectBody().json("updated_court".loadJson())

      assertThat(auditEventMessageCount()).isEqualTo(1)
      val auditMessage = auditMessage()
      JsonAssertions.assertThatJson(auditMessage).node("what").isEqualTo("COURT_REGISTER_UPDATE")
      JsonAssertions.assertThatJson(auditMessage).node("who").isEqualTo("bobby.beans")
      JsonAssertions.assertThatJson(auditMessage).node("service").isEqualTo("court-register")
      JsonAssertions.assertThatJson(auditMessage).node("details").isNotNull
      JsonAssertions.assertThatJson(auditMessage).node("when").asString().satisfies {
        val whenDateTime = LocalDateTime.parse(it)
        assertThat(whenDateTime).isCloseToUtcNow(within(5, ChronoUnit.SECONDS))
      }

    }

    @Test
    fun `update a court with bad data`() {
      webTestClient.put()
        .uri("/court-maintenance/id/ACCRYC")
        .accept(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation(roles = listOf("ROLE_MAINTAIN_REF_DATA"), scopes = listOf("write")))
        .body(
          BodyInserters.fromValue(
            mapOf(
              "courtName" to "A",
              "courtDescription" to "B",
              "courtType" to "DUMMY",
              "active" to "true"
            )
          )
        )
        .exchange()
        .expectStatus().isBadRequest
    }

    @Test
    fun `insert a court`() {
      whenever(courtRepository.findById("ACCRYD")).thenReturn(
        Optional.empty()
      )

      webTestClient.post()
        .uri("/court-maintenance")
        .accept(MediaType.APPLICATION_JSON)
        .headers(
          setAuthorisation(
            roles = listOf("ROLE_MAINTAIN_REF_DATA"),
            scopes = listOf("write"),
            user = "bobby.beans"
          )
        )
        .body(BodyInserters.fromValue(CourtDto("ACCRYD", "A New Court", "a description", YOUTH, true)))
        .exchange()
        .expectStatus().isCreated
        .expectBody().json("inserted_court".loadJson())

      assertThat(auditEventMessageCount()).isEqualTo(1)
      val auditMessage = auditMessage()
      JsonAssertions.assertThatJson(auditMessage).node("what").isEqualTo("COURT_REGISTER_INSERT")
      JsonAssertions.assertThatJson(auditMessage).node("who").isEqualTo("bobby.beans")
      JsonAssertions.assertThatJson(auditMessage).node("service").isEqualTo("court-register")
      JsonAssertions.assertThatJson(auditMessage).node("details").isNotNull
      JsonAssertions.assertThatJson(auditMessage).node("when").asString().satisfies {
        val whenDateTime = LocalDateTime.parse(it)
        assertThat(whenDateTime).isCloseToUtcNow(within(5, ChronoUnit.SECONDS))
      }
    }

    @Test
    fun `insert a court with bad data`() {
      webTestClient.post()
        .uri("/court-maintenance")
        .accept(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation(roles = listOf("ROLE_MAINTAIN_REF_DATA"), scopes = listOf("write")))
        .body(BodyInserters.fromValue(CourtDto("R", "A New Court", "a description", YOUTH, true)))
        .exchange()
        .expectStatus().isBadRequest
    }
  }

  @Suppress("ClassName")
  @Nested
  inner class findById {
    @Test
    fun `find court`() {
      val court = Court("ACCRYC", "Accrington Youth Court", null, YOUTH, true)

      whenever(courtRepository.findById(anyString())).thenReturn(
        Optional.of(court)
      )
      webTestClient.get().uri("/courts/id/ACCRYC")
        .exchange()
        .expectStatus().isOk
        .expectBody().json("court_id_ACCRYC".loadJson())
    }

    @Test
    fun `find court validation failure`() {
      webTestClient.get().uri("/courts/id/1234567890123")
        .exchange()
        .expectStatus().isBadRequest
        .expectBody().json("court_id_badrequest_getCourtFromId".loadJson())
    }
  }

  private fun String.loadJson(): String =
    CourtResourceTest::class.java.getResource("$this.json").readText()

  internal fun setAuthorisation(
    user: String = "court-reg-client",
    roles: List<String> = listOf(),
    scopes: List<String> = listOf()
  ): (HttpHeaders) -> Unit = jwtAuthHelper.setAuthorisation(user, roles, scopes)


  fun auditEventMessageCount(): Int? {
    val queueAttributes = awsSqsClient.getQueueAttributes(queueName.queueUrl(), listOf("ApproximateNumberOfMessages"))
    return queueAttributes.attributes["ApproximateNumberOfMessages"]?.toInt()
  }

  fun auditMessage(): String? {
    return awsSqsClient.receiveMessage(queueName.queueUrl()).messages.firstOrNull()?.body
  }

  fun String.queueUrl(): String = awsSqsClient.getQueueUrl(this).queueUrl

}
