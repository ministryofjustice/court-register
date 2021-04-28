package uk.gov.justice.digital.hmpps.courtregister.resource

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.courtregister.jpa.Court
import uk.gov.justice.digital.hmpps.courtregister.jpa.CourtRepository
import uk.gov.justice.digital.hmpps.courtregister.jpa.CourtType

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Transactional
class CourtResourcePagingTest : IntegrationTest() {

  companion object {
    val log = LoggerFactory.getLogger(this::class.java)
  }

  @Autowired
  private lateinit var courtRepository: CourtRepository

  private val testCourts = listOf(
    Court("AAAAAA", "AAAAAA New Court", "a description", CourtType("YTH", "Youth Court"), false),
    Court("AAAAAB", "AAAAAB New Court", "a description", CourtType("COU", "County Court"), true),
    Court("AAAAAC", "AAAAAC New Court", "a description", CourtType("CRN", "Crown Court"), true),
  )

  @BeforeAll
  fun `insert inactive test court`() {
    testCourts.forEach {
      courtRepository.save(it)
    }
  }

  @AfterAll
  fun `remove inactive test court`() {
    testCourts.forEach {
      courtRepository.delete(it)
    }
  }

  @Test
  fun `find page of courts`() {
    webTestClient.get().uri("/courts/paged?page=0&size=3&sort=courtName")
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .assertFirstPageOfMany()
      .jsonPath("$.content[0].courtId").isEqualTo("AAAAAA")
      .jsonPath("$.content[0].active").isEqualTo(false)
  }

  private fun WebTestClient.BodyContentSpec.assertFirstPageOfMany() =
    this.jsonPath("$.content.length()").isEqualTo(3)
      .jsonPath("$.size").isEqualTo(3)
      .jsonPath("$.totalElements").value<Int> { assertThat(it).isGreaterThan(3) }
      .jsonPath("$.totalPages").value<Int> { assertThat(it).isGreaterThan(1) }
      .jsonPath("$.last").isEqualTo(false)

  @Test
  fun `find page of active courts`() {
    webTestClient.get().uri("/courts/paged?page=0&size=3&sort=courtName&active=true")
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .assertFirstPageOfMany()
      .jsonPath("$.content[0].courtId").value<String> { assertThat(it).isNotEqualTo("AAAAAA") }
      .jsonPath("$.content[0].active").isEqualTo(true)
  }

  @Test
  fun `find page filtered by court type`() {
    webTestClient.get().uri("/courts/paged?page=0&size=3&sort=courtName&courtTypeIds=YTH")
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .assertFirstPageOfMany()
      .jsonPath("$.content[0].courtId").value<String> { assertThat(it).isEqualTo("AAAAAA") }
      .jsonPath("$.content[0].type.courtType").isEqualTo("YTH")
      .jsonPath("$.content[1].type.courtType").isEqualTo("YTH")
      .jsonPath("$.content[2].type.courtType").isEqualTo("YTH")
  }

  @Test
  fun `find page filtered by court type and active flag`() {
    webTestClient.get().uri("/courts/paged?page=0&size=3&sort=courtName&courtTypeIds=YTH&active=true")
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .assertFirstPageOfMany()
      .jsonPath("$.content[0].courtId").value<String> { assertThat(it).isNotEqualTo("AAAAAA") }
      .jsonPath("$.content[0].type.courtType").isEqualTo("YTH")
      .jsonPath("$.content[0].active").isEqualTo(true)
      .jsonPath("$.content[1].type.courtType").isEqualTo("YTH")
      .jsonPath("$.content[1].active").isEqualTo(true)
      .jsonPath("$.content[2].type.courtType").isEqualTo("YTH")
      .jsonPath("$.content[2].active").isEqualTo(true)
  }

  @Test
  fun `find page filtered by multiple court types`() {
    webTestClient.get().uri("/courts/paged?page=0&size=3&sort=courtName&courtTypeIds=YTH&courtTypeIds=CRN")
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .assertFirstPageOfMany()
      .jsonPath("$.content[0].courtId").value<String> { assertThat(it).isEqualTo("AAAAAA") }
      .jsonPath("$.content[1].courtId").value<String> { assertThat(it).isEqualTo("AAAAAC") }
      .jsonPath("$.content[2].type.courtType").value<String> { assertThat(it).isIn("YTH", "CRN") }
  }

  @Test
  fun `find page filtered by multiple court types and active flag`() {
    webTestClient.get().uri("/courts/paged?page=0&size=3&sort=courtName&courtTypeIds=YTH&courtTypeIds=CRN&active=true")
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .assertFirstPageOfMany()
      .jsonPath("$.content[0].courtId").value<String> { assertThat(it).isEqualTo("AAAAAC") }
      .jsonPath("$.content[1].type.courtType").value<String> { assertThat(it).isIn("YTH", "CRN") }
      .jsonPath("$.content[1].active").isEqualTo(true)
      .jsonPath("$.content[2].type.courtType").value<String> { assertThat(it).isIn("YTH", "CRN") }
      .jsonPath("$.content[2].active").isEqualTo(true)
  }
}
