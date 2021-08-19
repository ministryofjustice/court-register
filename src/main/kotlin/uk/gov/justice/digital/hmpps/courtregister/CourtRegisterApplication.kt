package uk.gov.justice.digital.hmpps.courtregister

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.aws.autoconfigure.messaging.MessagingAutoConfiguration

@SpringBootApplication(exclude = [MessagingAutoConfiguration::class])
class CourtRegisterApplication

fun main(args: Array<String>) {
  runApplication<CourtRegisterApplication>(*args)
}
