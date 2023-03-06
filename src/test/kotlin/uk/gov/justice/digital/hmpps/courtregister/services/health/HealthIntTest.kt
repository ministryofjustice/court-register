package uk.gov.justice.digital.hmpps.courtregister.services.health

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.courtregister.resource.IntegrationTest
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter.ISO_DATE
import java.util.function.Consumer

class HealthIntTest : IntegrationTest() {
  @Test
  fun `Health page reports ok`() {
    webTestClient.get().uri("/health")
      .exchange()
      .expectStatus().isOk
      .expectBody().jsonPath("status").isEqualTo("UP")
  }

  @Test
  fun `Health ping reports ok`() {
    webTestClient.get().uri("/health/ping")
      .exchange()
      .expectStatus().isOk
      .expectBody().jsonPath("status").isEqualTo("UP")
  }

  @Test
  fun `Health info reports version`() {
    webTestClient.get().uri("/health")
      .exchange()
      .expectStatus().isOk
      .expectBody().jsonPath("components.healthInfo.details.version").value(
        Consumer<String> {
          assertThat(it).startsWith(LocalDateTime.now().format(ISO_DATE))
        },
      )
  }

  @Test
  fun `Queue health reports queue details`() {
    webTestClient.get().uri("/health")
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .jsonPath("components.audit-health.status").isEqualTo("UP")
      .jsonPath("components.audit-health.details.queueName").isEqualTo(queueName)
      .jsonPath("components.audit-health.details.messagesOnQueue").isEqualTo(0)
      .jsonPath("components.audit-health.details.messagesInFlight").isEqualTo(0)
  }
}
