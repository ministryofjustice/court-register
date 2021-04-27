package uk.gov.justice.digital.hmpps.courtregister.resource

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.reactive.function.BodyInserters

@Transactional
class CourtBuildingMaintenanceIntTest : IntegrationTest() {
  @Nested
  @DisplayName("Updating court buildings")
  inner class UpdatingBuildings() {
    @Test
    fun `can update a building`() {
      webTestClient.put().uri("court-maintenance/id/ABDRCT/buildings/767")
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .headers(
          setAuthorisation(
            roles = listOf("ROLE_MAINTAIN_REF_DATA"),
            scopes = listOf("write"),
            user = "bobby.beans"
          )
        )
        .body(
          BodyInserters.fromValue(
            """
              {
                "buildingName": "New Crown Building",
                "street": "Green Street",
                "town": "M GLAM",
                "county": "Aberdare",
                "postcode": "CF44 7DW"
            }
        """.trimIndent()
          )
        )
        .exchange()
        .expectStatus().isOk

      webTestClient.get().uri("/courts/id/ABDRCT/buildings/id/767")
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.buildingName").isEqualTo("New Crown Building")
    }
    @Test
    fun `can update a building subcode`() {
      webTestClient.put().uri("court-maintenance/id/ABDRCT/buildings/767")
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .headers(
          setAuthorisation(
            roles = listOf("ROLE_MAINTAIN_REF_DATA"),
            scopes = listOf("write"),
            user = "bobby.beans"
          )
        )
        .body(
          BodyInserters.fromValue(
            """
              {
                "buildingName": "Crown Building",
                "subCode": "ZYXAA",
                "street": "Green Street",
                "town": "M GLAM",
                "county": "Aberdare",
                "postcode": "CF44 7DW"
            }
        """.trimIndent()
          )
        )
        .exchange()
        .expectStatus().isOk

      webTestClient.get().uri("/courts/id/ABDRCT/buildings/id/767")
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.subCode").isEqualTo("ZYXAA")
    }
    @Test
    fun `can not update a building sub-code to existing court code`() {
      webTestClient.put().uri("court-maintenance/id/ABDRCT/buildings/767")
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .headers(
          setAuthorisation(
            roles = listOf("ROLE_MAINTAIN_REF_DATA"),
            scopes = listOf("write"),
            user = "bobby.beans"
          )
        )
        .body(
          BodyInserters.fromValue(
            """
              {
                "buildingName": "Crown Building",
                "subCode": "BRMNCC",
                "street": "Green Street",
                "town": "M GLAM",
                "county": "Aberdare",
                "postcode": "CF44 7DW"
            }
        """.trimIndent()
          )
        )
        .exchange()
        .expectStatus().isEqualTo(HttpStatus.CONFLICT)
    }
    @Test
    fun `can not update a building sub-code to existing building sub-code`() {
      webTestClient.put().uri("court-maintenance/id/ABDRCT/buildings/767")
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .headers(
          setAuthorisation(
            roles = listOf("ROLE_MAINTAIN_REF_DATA"),
            scopes = listOf("write"),
            user = "bobby.beans"
          )
        )
        .body(
          BodyInserters.fromValue(
            """
              {
                "buildingName": "Crown Building",
                "subCode": "BCCACC",
                "street": "Green Street",
                "town": "M GLAM",
                "county": "Aberdare",
                "postcode": "CF44 7DW"
            }
        """.trimIndent()
          )
        )
        .exchange()
        .expectStatus().isEqualTo(HttpStatus.CONFLICT)
    }
  }
}


