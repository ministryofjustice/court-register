package uk.gov.justice.digital.hmpps.courtregister.resource

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.courtregister.jpa.Court
import uk.gov.justice.digital.hmpps.courtregister.jpa.CourtRepository
import uk.gov.justice.digital.hmpps.courtregister.jpa.CourtType

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Transactional
class CourtResourcePagingTest : IntegrationTest() {

  @Autowired
  private lateinit var courtRepository: CourtRepository

  private val inactiveTestCourt = Court("AAAAAA", "A New Court", "a description", CourtType("YTH", "Youth Court"), false)

  @BeforeAll
  fun `insert inactive test court`() {
    courtRepository.save(inactiveTestCourt)
  }

  @Test
  fun `find page of courts`() {
    webTestClient.get().uri("/courts/all/paged?page=0&size=3&sort=courtName")
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .jsonPath("$.content.length()").isEqualTo(3)
      .jsonPath("$.size").isEqualTo(3)
      .jsonPath("$.totalElements").value<Int> { assertThat(it).isGreaterThan(3) }
      .jsonPath("$.totalPages").value<Int> { assertThat(it).isGreaterThan(1) }
      .jsonPath("$.last").isEqualTo(false)
      .jsonPath("$.content[0].courtId").isEqualTo("AAAAAA")
      .jsonPath("$.content[0].active").isEqualTo(false)
  }

  @Test
  fun `find page of active courts`() {
    webTestClient.get().uri("/courts/all/paged?page=0&size=3&sort=courtName&active=true")
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .jsonPath("$.content.length()").isEqualTo(3)
      .jsonPath("$.size").isEqualTo(3)
      .jsonPath("$.totalElements").value<Int> { assertThat(it).isGreaterThan(3) }
      .jsonPath("$.totalPages").value<Int> { assertThat(it).isGreaterThan(1) }
      .jsonPath("$.last").isEqualTo(false)
      .jsonPath("$.content[0].courtId").value<String> { assertThat(it).isNotEqualTo("AAAAAA") }
      .jsonPath("$.content[0].active").isEqualTo(true)
  }

  @Test
  fun `find page filtered by court type`() {
    webTestClient.get().uri("/courts/all/paged?page=0&size=3&sort=courtName&courtTypeId=YTH")
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .jsonPath("$.content.length()").isEqualTo(3)
      .jsonPath("$.size").isEqualTo(3)
      .jsonPath("$.totalElements").value<Int> { assertThat(it).isGreaterThan(3) }
      .jsonPath("$.totalPages").value<Int> { assertThat(it).isGreaterThan(1) }
      .jsonPath("$.last").isEqualTo(false)
      .jsonPath("$.content[0].courtId").value<String> { assertThat(it).isEqualTo("AAAAAA") }
      .jsonPath("$.content[0].type.courtType").isEqualTo("YTH")
      .jsonPath("$.content[1].type.courtType").isEqualTo("YTH")
      .jsonPath("$.content[2].type.courtType").isEqualTo("YTH")
  }

  @Test
  fun `find page filtered by court type and active flag`() {
    webTestClient.get().uri("/courts/all/paged?page=0&size=3&sort=courtName&courtTypeId=YTH&active=true")
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .jsonPath("$.content.length()").isEqualTo(3)
      .jsonPath("$.size").isEqualTo(3)
      .jsonPath("$.totalElements").value<Int> { assertThat(it).isGreaterThan(3) }
      .jsonPath("$.totalPages").value<Int> { assertThat(it).isGreaterThan(1) }
      .jsonPath("$.last").isEqualTo(false)
      .jsonPath("$.content[0].courtId").value<String> { assertThat(it).isNotEqualTo("AAAAAA") }
      .jsonPath("$.content[0].type.courtType").isEqualTo("YTH")
      .jsonPath("$.content[0].active").isEqualTo(true)
      .jsonPath("$.content[1].type.courtType").isEqualTo("YTH")
      .jsonPath("$.content[1].active").isEqualTo(true)
      .jsonPath("$.content[2].type.courtType").isEqualTo("YTH")
      .jsonPath("$.content[2].active").isEqualTo(true)
  }
}
