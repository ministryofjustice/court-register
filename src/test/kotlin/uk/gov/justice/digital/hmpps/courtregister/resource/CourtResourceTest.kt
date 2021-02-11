package uk.gov.justice.digital.hmpps.courtregister.resource

import com.nhaarman.mockito_kotlin.whenever
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.BodyInserters
import uk.gov.justice.digital.hmpps.courtregister.helper.JwtAuthHelper
import uk.gov.justice.digital.hmpps.courtregister.jpa.Court
import uk.gov.justice.digital.hmpps.courtregister.jpa.CourtRepository
import java.util.Optional

class CourtResourceTest : IntegrationTest() {
  @MockBean
  private lateinit var courtRepository: CourtRepository

  @Autowired
  protected lateinit var jwtAuthHelper: JwtAuthHelper

  @Suppress("ClassName")
  @Nested
  inner class findAll {
    @Test
    fun `find active courts`() {
      val courts = listOf(
        Court("ACCRYC", "Accrington Youth Court", null, "Youth Court", true),
        Court("KIDDYC", "Kidderminster Youth Court", null, "Youth Court", true)
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
        Court("ACCRYC", "Accrington Youth Court", null, "Youth Court", true),
        Court("KIDDYC", "Kidderminster Youth Court", null, "Youth Court", true),
        Court("KIDDYE", "Kidderminster Crown Court", null, "Crown Court", false)
      )

      whenever(courtRepository.findAll()).thenReturn(
        courts
      )
      webTestClient.get().uri("/courts/all")
        .exchange()
        .expectStatus().isOk
        .expectBody().json("courts_all".loadJson())
    }
  }

  @Suppress("ClassName")
  @Nested
  inner class updateAndInsertCourts {

    @Test
    fun `correct permission are neeed to update court data`() {
      webTestClient.put()
        .uri("/court-maintenance/id/ACCRYC")
        .accept(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation(roles = listOf("ROLE_DUMMY"), scopes = listOf("write")))
        .body(BodyInserters.fromValue(UpdateCourtDto("Updated Court", "a description", "Youth Court", false)))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `correct scopes are neeed to update court data`() {
      webTestClient.put()
        .uri("/court-maintenance/id/ACCRYC")
        .accept(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation(roles = listOf("ROLE_MAINTAIN_REF_DATA"), scopes = listOf("read")))
        .body(BodyInserters.fromValue(UpdateCourtDto("Updated Court", "a description", "Youth Court", false)))
        .exchange().expectStatus().isForbidden
    }

    @Test
    fun `update a court`() {
      whenever(courtRepository.findById("ACCRYC")).thenReturn(
        Optional.of(Court("ACCRYC", "A Court 1", null, "Crown", true))
      )

      webTestClient.put()
        .uri("/court-maintenance/id/ACCRYC")
        .accept(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation(roles = listOf("ROLE_MAINTAIN_REF_DATA"), scopes = listOf("write")))
        .body(BodyInserters.fromValue(UpdateCourtDto("Updated Court", "a description", "Youth Court", false)))
        .exchange()
        .expectStatus().isOk
        .expectBody().json("updated_court".loadJson())
    }

    @Test
    fun `insert a court`() {
      whenever(courtRepository.findById("ACCRYD")).thenReturn(
        Optional.empty()
      )

      webTestClient.post()
        .uri("/court-maintenance")
        .accept(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation(roles = listOf("ROLE_MAINTAIN_REF_DATA"), scopes = listOf("write")))
        .body(BodyInserters.fromValue(CourtDto("ACCRYD", "A New Court", "a description", "Youth Court", true)))
        .exchange()
        .expectStatus().isCreated
        .expectBody().json("inserted_court".loadJson())
    }
  }

  @Suppress("ClassName")
  @Nested
  inner class findById {
    @Test
    fun `find court`() {
      val court = Court("ACCRYC", "Accrington Youth Court", null, "Youth Court", true)

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
}
