package uk.gov.justice.digital.hmpps.courtregister.resource

import com.amazonaws.services.sqs.AmazonSQSAsync
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.reactive.server.WebTestClient
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.wait.strategy.Wait
import uk.gov.justice.digital.hmpps.courtregister.helper.FlywayRestoreExtension
import uk.gov.justice.digital.hmpps.courtregister.helper.JwtAuthHelper
import java.io.IOException
import java.net.ServerSocket

object PostgresqlContainer {
  val instance: PostgreSQLContainer<Nothing>? by lazy { startPostgresqlContainer() }
  fun startPostgresqlContainer(): PostgreSQLContainer<Nothing>? =
    if (checkPostgresRunning().not()) {
      PostgreSQLContainer<Nothing>("postgres").apply {
        withEnv("HOSTNAME_EXTERNAL", "localhost")
        withExposedPorts(5432)
        withDatabaseName("court_register_db")
        withUsername("admin")
        withPassword("admin_password")
        setWaitStrategy(Wait.forListeningPort())
        withReuse(true)
        start()
      }
    } else {
      null
    }
}

private fun checkPostgresRunning(): Boolean =
  try {
    val serverSocket: ServerSocket = ServerSocket(5432)
    serverSocket.localPort == 0
  } catch (e: IOException) {
    true
  }

@ExtendWith(FlywayRestoreExtension::class)
@Suppress("SpringJavaInjectionPointsAutowiringInspection")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
abstract class IntegrationTest {
  @Suppress("unused")
  @Autowired
  lateinit var webTestClient: WebTestClient

  @Autowired
  protected lateinit var awsSqsClient: AmazonSQSAsync

  @Autowired
  protected lateinit var jwtAuthHelper: JwtAuthHelper

  @Value("\${sqs.queue.name}")
  protected lateinit var queueName: String

  init {
    // Resolves an issue where Wiremock keeps previous sockets open from other tests causing connection resets
    System.setProperty("http.keepAlive", "false")
  }

  fun String.queueUrl(): String = awsSqsClient.getQueueUrl(this).queueUrl

  internal fun setAuthorisation(
    user: String = "court-reg-client",
    roles: List<String> = listOf(),
    scopes: List<String> = listOf()
  ): (HttpHeaders) -> Unit = jwtAuthHelper.setAuthorisation(user, roles, scopes)

  companion object {
    private val pgContainer = PostgresqlContainer.instance

    @JvmStatic
    @DynamicPropertySource
    fun properties(registry: DynamicPropertyRegistry) {
      pgContainer?.run {
        registry.add("spring.datasource.url", pgContainer::getJdbcUrl)
        registry.add("spring.datasource.username", pgContainer::getUsername)
        registry.add("spring.datasource.password", pgContainer::getPassword)
        registry.add("spring.datasource.placeholders.database_update_password", pgContainer::getPassword)
        registry.add("spring.datasource.placeholders.database_read_only_password", pgContainer::getPassword)
        registry.add("spring.jpa.properties.hibernate.default_schema", pgContainer::getDatabaseName)
        registry.add("spring.flyway.url", pgContainer::getJdbcUrl)
        registry.add("spring.flyway.user", pgContainer::getUsername)
        registry.add("spring.flyway.password", pgContainer::getPassword)
        registry.add("spring.flyway.schemas", pgContainer::getDatabaseName)
      }
    }
  }
}
