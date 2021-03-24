package uk.gov.justice.digital.hmpps.courtregister.resource

import com.amazonaws.services.sqs.model.PurgeQueueRequest
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.check
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import net.javacrumbs.jsonunit.assertj.JsonAssertions
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.within
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.expectBody
import org.springframework.web.reactive.function.BodyInserters
import uk.gov.justice.digital.hmpps.courtregister.helper.JwtAuthHelper
import uk.gov.justice.digital.hmpps.courtregister.jpa.Building
import uk.gov.justice.digital.hmpps.courtregister.jpa.BuildingRepository
import uk.gov.justice.digital.hmpps.courtregister.jpa.Contact
import uk.gov.justice.digital.hmpps.courtregister.jpa.ContactRepository
import uk.gov.justice.digital.hmpps.courtregister.jpa.Court
import uk.gov.justice.digital.hmpps.courtregister.jpa.CourtRepository
import uk.gov.justice.digital.hmpps.courtregister.jpa.CourtType
import uk.gov.justice.digital.hmpps.courtregister.jpa.CourtTypeRepository
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import java.util.Optional

@Suppress("ClassName")
class CourtResourceTest : IntegrationTest() {
  @MockBean
  private lateinit var courtRepository: CourtRepository

  @MockBean
  private lateinit var buildingRepository: BuildingRepository

  @MockBean
  private lateinit var contactRepository: ContactRepository

  @MockBean
  private lateinit var courtTypeRepository: CourtTypeRepository

  @Autowired
  protected lateinit var jwtAuthHelper: JwtAuthHelper

  @Nested
  inner class findAll {
    @Test
    fun `find active courts`() {
      val courts = listOf(
        Court("ACCRYC", "Accrington Youth Court", null, CourtType("YOUTH", "Youth Court"), true),
        Court("KIDDYC", "Kidderminster Youth Court", null, CourtType("YOUTH", "Youth Court"), true)
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
    fun `find page of active courts`() {
      val courts = listOf(
        Court("ACCRYC", "Accrington Youth Court", null, CourtType("YTH", "Youth Court"), true),
        Court("BOSTMC", "Boston Magistrates Court", null, CourtType("CRN", "Crown Court"), true),
        Court("BRNTCT", "Barnet County Court", null, CourtType("COU", "County Court"), true),
      )
      whenever(courtRepository.findByActiveOrderById(eq(true), any())).thenReturn(PageImpl(courts))

      val test = webTestClient.get().uri("/courts/paged?page=0&size=3")
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.content[0].courtId").isEqualTo("ACCRYC")
        .jsonPath("$.content[0].courtName").isEqualTo("Accrington Youth Court")
        .jsonPath("$.content[0].courtType").isEqualTo("YTH")
        .jsonPath("$.content[0].active").isEqualTo(true)
        .jsonPath("$.content[1].courtId").isEqualTo("BOSTMC")
        .jsonPath("$.content[2].courtId").isEqualTo("BRNTCT")

      verify(courtRepository).findByActiveOrderById(eq(true), check {
        assertThat(it.pageNumber).isEqualTo(0)
        assertThat(it.pageSize).isEqualTo(3)
      })
    }

    @Test
    fun `find all courts`() {
      val courts = listOf(
        Court("ACCRYC", "Accrington Youth Court", null, CourtType("YOUTH", "Youth Court"), true),
        Court("KIDDYC", "Kidderminster Youth Court", null, CourtType("YOUTH", "Youth Court"), true),
        Court("KIDDYE", "Kidderminster Crown Court", null, CourtType("CROWN", "Crown Court"), false)
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
    fun `find page of courts`() {
      val courts = listOf(
        Court("ACCRYC", "Accrington Youth Court", null, CourtType("YTH", "Youth Court"), false),
        Court("BOSTMC", "Boston Magistrates Court", null, CourtType("CRN", "Crown Court"), false),
        Court("BRNTCT", "Barnet County Court", null, CourtType("COU", "County Court"), true),
      )
      whenever(courtRepository.findByActiveOrderById(eq(true), any())).thenReturn(PageImpl(courts))

      val test = webTestClient.get().uri("/courts/all/paged?page=0&size=3")
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.content[0].courtId").isEqualTo("ACCRYC")
        .jsonPath("$.content[0].courtName").isEqualTo("Accrington Youth Court")
        .jsonPath("$.content[0].courtType").isEqualTo("YTH")
        .jsonPath("$.content[0].active").isEqualTo(false)
        .jsonPath("$.content[1].courtId").isEqualTo("BOSTMC")
        .jsonPath("$.content[2].courtId").isEqualTo("BRNTCT")

      verify(courtRepository).findByActiveOrderById(eq(true), check {
        assertThat(it.pageNumber).isEqualTo(0)
        assertThat(it.pageSize).isEqualTo(3)
      })
    }

    @Test
    fun `find courts types`() {
      whenever(courtTypeRepository.findAll()).thenReturn(
        listOf(
          CourtType("CROWN", "Crown Court"),
          CourtType("YOUTH", "Youth Court"),
        )
      )

      webTestClient.get().uri("/courts/types")
        .exchange()
        .expectStatus().isOk
        .expectBody().json("court_types".loadJson())
    }
  }

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
        .body(BodyInserters.fromValue(UpdateCourtDto("Updated Court", "a description", "YOUTH", false)))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `correct scopes are needed to update court data`() {
      webTestClient.put()
        .uri("/court-maintenance/id/ACCRYC")
        .accept(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation(roles = listOf("ROLE_MAINTAIN_REF_DATA"), scopes = listOf("read")))
        .body(BodyInserters.fromValue(UpdateCourtDto("Updated Court", "a description", "YOUTH", false)))
        .exchange().expectStatus().isForbidden
    }

    @Test
    fun `update a court`() {
      whenever(courtRepository.findById("ACCRYC")).thenReturn(
        Optional.of(Court("ACCRYC", "A Court 1", null, CourtType("YOUTH", "Youth Court"), true))
      )
      whenever(courtTypeRepository.findById("YOUTH")).thenReturn(Optional.of(CourtType("YOUTH", "Youth Court")))
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
        .body(BodyInserters.fromValue(UpdateCourtDto("Updated Court", "a description", "YOUTH", false)))
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
        val whenDateTime = LocalDateTime.ofInstant(Instant.parse(it), ZoneOffset.UTC)
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
      whenever(courtTypeRepository.findById("YOUTH")).thenReturn(Optional.of(CourtType("YOUTH", "Youth Court")))
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
        .body(BodyInserters.fromValue(CourtDto("ACCRYD", "A New Court", "a description", CourtTypeDto("YOUTH", "Youth Court"), true)))
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
        val whenDateTime = LocalDateTime.ofInstant(Instant.parse(it), ZoneOffset.UTC)
        assertThat(whenDateTime).isCloseToUtcNow(within(5, ChronoUnit.SECONDS))
      }
    }

    @Test
    fun `insert a court with bad data`() {
      webTestClient.post()
        .uri("/court-maintenance")
        .accept(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation(roles = listOf("ROLE_MAINTAIN_REF_DATA"), scopes = listOf("write")))
        .body(BodyInserters.fromValue(CourtDto("R", "A New Court", "a description", CourtTypeDto("YOUTH", "Youth Court"), true)))
        .exchange()
        .expectStatus().isBadRequest
    }
  }

  @Nested
  inner class findById {
    @Test
    fun `find court`() {
      val court = Court("ACCRYC", "Accrington Youth Court", null, CourtType("YOUTH", "Youth Court"), true)
      val building1 = Building(
        id = 1,
        court = court,
        subCode = "SUBT111",
        street = "West Cross",
        buildingName = "Annex",
        locality = "Mumble",
        town = "Sheffield",
        postcode = "SA4 5TH",
        county = "Yorkshire",
        country = "UK"
      )
      val building2 = Building(
        id = 2,
        court = court,
        subCode = null,
        street = "West Cross",
        buildingName = "Main building",
        locality = "A Place",
        town = "Sheffield",
        postcode = "SA4 5TT",
        county = "Yorkshire",
        country = "UK"
      )

      court.buildings?.add(building1)
      court.buildings?.add(building2)

      val contact1 = Contact(-1, building1, "TEL", "555 666666")
      val contact2 = Contact(-2, building1, "EMAIL", "test@test.com")
      val contact3 = Contact(-3, building2, "TEL", "555 6666655")

      building1.contacts?.add(contact1)
      building1.contacts?.add(contact2)
      building2.contacts?.add(contact3)

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

  @Nested
  inner class updateAndInsertBuildings {

    @BeforeEach
    internal fun drainAuditQueue() {
      awsSqsClient.purgeQueue(PurgeQueueRequest(queueName.queueUrl()))
    }

    @Test
    fun `correct permission are needed to update building data`() {
      webTestClient.put()
        .uri("/court-maintenance/id/ACCRYC/buildings/1")
        .accept(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation(roles = listOf("ROLE_DUMMY"), scopes = listOf("write")))
        .body(
          BodyInserters.fromValue(
            UpdateBuildingDto(
              subCode = "SUBT111",
              street = "West Cross",
              buildingName = "Annex",
              locality = "Mumble",
              town = "Sheffield",
              postcode = "SA4 5TH",
              county = "Yorkshire",
              country = "UK"
            )
          )
        )
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `correct scopes are needed to update building data`() {
      webTestClient.put()
        .uri("/court-maintenance/id/ACCRYC/buildings/1")
        .accept(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation(roles = listOf("ROLE_MAINTAIN_REF_DATA"), scopes = listOf("read")))
        .body(
          BodyInserters.fromValue(
            UpdateBuildingDto(
              subCode = "SUBT111",
              street = "West Cross",
              buildingName = "Annex",
              locality = "Mumble",
              town = "Sheffield",
              postcode = "SA4 5TH",
              county = "Yorkshire",
              country = "UK"
            )
          )
        )
        .exchange().expectStatus().isForbidden
    }

    @Test
    fun `update a building`() {
      whenever(buildingRepository.findById(1)).thenReturn(
        Optional.of(
          Building(
            id = 1,
            court = Court("ACCRYC", "Accrington Youth Court", null, CourtType("YOUTH", "Youth Court"), true),
            subCode = "SUBBUILD1",
            street = "West Cross",
            buildingName = "Annex",
            locality = "Yorkshire",
            town = "Sheffield",
            postcode = "S11 9BQ",
            county = "South Yorkshire",
            country = "UK"
          )
        )
      )

      whenever(buildingRepository.findBySubCode("SUBT111")).thenReturn(Optional.empty())

      webTestClient.put()
        .uri("/court-maintenance/id/ACCRYC/buildings/1")
        .accept(MediaType.APPLICATION_JSON)
        .headers(
          setAuthorisation(
            roles = listOf("ROLE_MAINTAIN_REF_DATA"),
            scopes = listOf("write"),
            user = "bobby.beans"
          )
        )
        .body(
          BodyInserters.fromValue(
            UpdateBuildingDto(
              subCode = "SUBT111",
              street = "West Cross",
              buildingName = "Annex",
              locality = "Mumble",
              town = "Sheffield",
              postcode = "SA4 5TH",
              county = "Yorkshire",
              country = "UK"
            )
          )
        )
        .exchange()
        .expectStatus().isOk
        .expectBody().json("updated_building".loadJson())

      assertThat(auditEventMessageCount()).isEqualTo(1)
      val auditMessage = auditMessage()
      JsonAssertions.assertThatJson(auditMessage).node("what").isEqualTo("COURT_REGISTER_BUILDING_UPDATE")
      JsonAssertions.assertThatJson(auditMessage).node("who").isEqualTo("bobby.beans")
      JsonAssertions.assertThatJson(auditMessage).node("service").isEqualTo("court-register")
      JsonAssertions.assertThatJson(auditMessage).node("details").isNotNull
      JsonAssertions.assertThatJson(auditMessage).node("when").asString().satisfies {
        val whenDateTime = LocalDateTime.ofInstant(Instant.parse(it), ZoneOffset.UTC)
        assertThat(whenDateTime).isCloseToUtcNow(within(5, ChronoUnit.SECONDS))
      }
    }

    @Test
    fun `insert a building`() {
      val court = Court("ACCRYC", "Accrington Youth Court", null, CourtType("YOUTH", "Youth Court"), true)
      whenever(courtRepository.findById("ACCRYD")).thenReturn(
        Optional.of(court)
      )

      val createdBuilding = Building(
        court = court,
        subCode = "SUBT111",
        street = "West Cross",
        buildingName = "Annex",
        locality = "Mumble",
        town = "Sheffield",
        postcode = "SA4 5TH",
        county = "Yorkshire",
        country = "UK"
      )

      val updatedBuilding = createdBuilding.copy(id = 1)

      whenever(buildingRepository.save(createdBuilding)).thenReturn(
        updatedBuilding
      )

      webTestClient.post()
        .uri("/court-maintenance/id/ACCRYD/buildings")
        .accept(MediaType.APPLICATION_JSON)
        .headers(
          setAuthorisation(
            roles = listOf("ROLE_MAINTAIN_REF_DATA"),
            scopes = listOf("write"),
            user = "bobby.beans"
          )
        )
        .body(
          BodyInserters.fromValue(
            UpdateBuildingDto(
              subCode = "SUBT111",
              street = "West Cross",
              buildingName = "Annex",
              locality = "Mumble",
              town = "Sheffield",
              postcode = "SA4 5TH",
              county = "Yorkshire",
              country = "UK"
            )
          )
        )
        .exchange()
        .expectStatus().isCreated
        .expectBody().json("inserted_building".loadJson())

      assertThat(auditEventMessageCount()).isEqualTo(1)
      val auditMessage = auditMessage()
      JsonAssertions.assertThatJson(auditMessage).node("what").isEqualTo("COURT_REGISTER_BUILDING_INSERT")
      JsonAssertions.assertThatJson(auditMessage).node("who").isEqualTo("bobby.beans")
      JsonAssertions.assertThatJson(auditMessage).node("service").isEqualTo("court-register")
      JsonAssertions.assertThatJson(auditMessage).node("details").isNotNull
      JsonAssertions.assertThatJson(auditMessage).node("when").asString().satisfies {
        val whenDateTime = LocalDateTime.ofInstant(Instant.parse(it), ZoneOffset.UTC)
        assertThat(whenDateTime).isCloseToUtcNow(within(5, ChronoUnit.SECONDS))
      }
    }
  }

  @Nested
  inner class updateAndInsertContacts {

    @BeforeEach
    internal fun drainAuditQueue() {
      awsSqsClient.purgeQueue(PurgeQueueRequest(queueName.queueUrl()))
    }

    @Test
    fun `correct permission are needed to update contact data`() {
      webTestClient.put()
        .uri("/court-maintenance/id/ACCRYC/buildings/1/contacts/1")
        .accept(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation(roles = listOf("ROLE_DUMMY"), scopes = listOf("write")))
        .body(BodyInserters.fromValue(UpdateContactDto("TEL", "5555 666666")))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `correct scopes are needed to update contact data`() {
      webTestClient.put()
        .uri("/court-maintenance/id/ACCRYC/buildings/1/contacts/1")
        .accept(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation(roles = listOf("ROLE_MAINTAIN_REF_DATA"), scopes = listOf("read")))
        .body(BodyInserters.fromValue(UpdateContactDto("TEL", "5555 666666")))
        .exchange().expectStatus().isForbidden
    }

    @Test
    fun `update a contact`() {
      val court = Court("ACCRYC", "Accrington Youth Court", null, CourtType("YOUTH", "Youth Court"), true)
      val building = Building(
        id = 1,
        court = court,
        subCode = "SUBT111",
        street = "West Cross",
        buildingName = "Annex",
        locality = "Mumble",
        town = "Sheffield",
        postcode = "SA4 5TH",
        county = "Yorkshire",
        country = "UK"
      )

      whenever(contactRepository.findById(1)).thenReturn(
        Optional.of(Contact(building = building, type = "TEL", detail = "5555 6666666", id = 1))
      )

      webTestClient.put()
        .uri("/court-maintenance/id/ACCRYC/buildings/1/contacts/1")
        .accept(MediaType.APPLICATION_JSON)
        .headers(
          setAuthorisation(
            roles = listOf("ROLE_MAINTAIN_REF_DATA"),
            scopes = listOf("write"),
            user = "bobby.beans"
          )
        )
        .body(BodyInserters.fromValue(UpdateContactDto("TEL", "7777 22222222")))
        .exchange()
        .expectStatus().isOk
        .expectBody().json("updated_contact".loadJson())

      assertThat(auditEventMessageCount()).isEqualTo(1)
      val auditMessage = auditMessage()
      JsonAssertions.assertThatJson(auditMessage).node("what").isEqualTo("COURT_REGISTER_CONTACT_UPDATE")
      JsonAssertions.assertThatJson(auditMessage).node("who").isEqualTo("bobby.beans")
      JsonAssertions.assertThatJson(auditMessage).node("service").isEqualTo("court-register")
      JsonAssertions.assertThatJson(auditMessage).node("details").isNotNull
      JsonAssertions.assertThatJson(auditMessage).node("when").asString().satisfies {
        val whenDateTime = LocalDateTime.ofInstant(Instant.parse(it), ZoneOffset.UTC)
        assertThat(whenDateTime).isCloseToUtcNow(within(5, ChronoUnit.SECONDS))
      }
    }

    @Test
    fun `insert a contact`() {
      val court = Court("ACCRYC", "Accrington Youth Court", null, CourtType("YOUTH", "Youth Court"), true)
      val building = Building(
        id = 1,
        court = court,
        subCode = "SUBT111",
        street = "West Cross",
        buildingName = "Annex",
        locality = "Mumble",
        town = "Sheffield",
        postcode = "SA4 5TH",
        county = "Yorkshire",
        country = "UK"
      )
      building.contacts?.add(Contact(id = 1, type = "TEL", detail = "5555 33333", building = building))

      whenever(buildingRepository.findById(1)).thenReturn(
        Optional.of(building)
      )

      val contactToSave = Contact(type = "EMAIL", detail = "test@test.com", building = building)

      whenever(contactRepository.save(contactToSave)).thenReturn(
        contactToSave.copy(id = 2)
      )

      webTestClient.post()
        .uri("/court-maintenance/id/ACCRYC/buildings/1/contacts")
        .accept(MediaType.APPLICATION_JSON)
        .headers(
          setAuthorisation(
            roles = listOf("ROLE_MAINTAIN_REF_DATA"),
            scopes = listOf("write"),
            user = "bobby.beans"
          )
        )
        .body(BodyInserters.fromValue(UpdateContactDto("EMAIL", "test@test.com")))
        .exchange()
        .expectStatus().isCreated
        .expectBody().json("inserted_contact".loadJson())

      assertThat(auditEventMessageCount()).isEqualTo(1)
      val auditMessage = auditMessage()
      JsonAssertions.assertThatJson(auditMessage).node("what").isEqualTo("COURT_REGISTER_CONTACT_INSERT")
      JsonAssertions.assertThatJson(auditMessage).node("who").isEqualTo("bobby.beans")
      JsonAssertions.assertThatJson(auditMessage).node("service").isEqualTo("court-register")
      JsonAssertions.assertThatJson(auditMessage).node("details").isNotNull
      JsonAssertions.assertThatJson(auditMessage).node("when").asString().satisfies {
        val whenDateTime = LocalDateTime.ofInstant(Instant.parse(it), ZoneOffset.UTC)
        assertThat(whenDateTime).isCloseToUtcNow(within(5, ChronoUnit.SECONDS))
      }
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
}
