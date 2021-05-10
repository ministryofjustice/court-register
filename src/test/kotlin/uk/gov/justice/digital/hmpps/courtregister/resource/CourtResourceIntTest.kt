package uk.gov.justice.digital.hmpps.courtregister.resource

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class CourtResourceIntTest : IntegrationTest() {

  @Nested
  inner class getBuildingByCourtIdAndSubCode {

    @Test
    fun `should find a building by court ID and subCode`() {
      webTestClient.get().uri("/courts/id/BRMNCC/subcodes/BCCACC")
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("courtId").isEqualTo("BRMNCC")
        .jsonPath("subCode").isEqualTo("BCCACC")
    }

    @Test
    fun `should find a building by court ID and null subCode`() {
      webTestClient.get().uri("/courts/id/BRMNCC/subcodes")
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("courtId").isEqualTo("BRMNCC")
        .jsonPath("subCode").doesNotExist()
    }

    @Test
    fun `should not find a building by invalid court ID`() {
      webTestClient.get().uri("/courts/id/BAD/subcodes")
        .exchange()
        .expectStatus().isNotFound
    }

    @Test
    fun `should not find a building by court ID and invalid subCode`() {
      webTestClient.get().uri("/courts/id/BRMNCC/subcodes/BAD")
        .exchange()
        .expectStatus().isNotFound
    }
  }
}
