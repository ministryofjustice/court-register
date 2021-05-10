package uk.gov.justice.digital.hmpps.courtregister.resource

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class CourtResourceIntTest : IntegrationTest() {

  @Nested
  inner class FindMainBuilding {

    @Test
    fun `should find main building by court ID`() {
      webTestClient.get().uri("/courts/id/BRMNCC/buildings/main")
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("courtId").isEqualTo("BRMNCC")
        .jsonPath("subCode").doesNotExist()
    }

    @Test
    fun `should not find a building by invalid court ID`() {
      webTestClient.get().uri("/courts/id/BAD/bulidings/main")
        .exchange()
        .expectStatus().isNotFound
    }
  }
}
