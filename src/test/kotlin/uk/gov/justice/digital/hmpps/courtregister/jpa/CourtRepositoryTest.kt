package uk.gov.justice.digital.hmpps.courtregister.jpa

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.test.context.transaction.TestTransaction
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.courtregister.resource.IntegrationTest

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Transactional
class CourtRepositoryTest : IntegrationTest() {
  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  @Autowired
  lateinit var courtRepository: CourtRepository

  @Autowired
  lateinit var courtTypeRepository: CourtTypeRepository

  @AfterAll
  fun `delete added test data`() {
    courtRepository.findById("SHFCRT").ifPresent {
      courtRepository.delete(it)
    }
  }

  @Test
  fun `should load court data`() {
    val court = courtRepository.findById("ABRYMC").orElseThrow()

    with(court) {
      assertThat(id).isEqualTo("ABRYMC")
      assertThat(courtType.description).isEqualTo("Magistrates Court")
      assertThat(buildings[0].buildingName).isEqualTo("Swyddfa'r Sir")
      assertThat(buildings[0].contacts[0].detail).isEqualTo("01633 645000")
    }
  }

  @Test
  fun `should insert court`() {
    val crownCourtType = courtTypeRepository.findById("CRN").orElseThrow()

    val court = Court(
      id = "SHFCRT",
      courtName = "Sheffield Court",
      courtDescription = "A Court in Sheffield",
      courtType = crownCourtType,
      active = true
    )

    val building = Building(
      court = court,
      subCode = "AAASSS",
      street = "West Cross",
      buildingName = "Annex",
      locality = "Mumble",
      town = "Sheffield",
      postcode = "SA4 5TH",
      county = "Glamorgan",
      country = "UK",
      active = true
    )

    val contact = Contact(building = building, type = "TEL", detail = "55512121")

    building.contacts = building.contacts.plus(contact)
    court.buildings = court.buildings.plus(building)

    val id = courtRepository.save(court).id

    TestTransaction.flagForCommit()
    TestTransaction.end()

    TestTransaction.start()
    val savedCourt = courtRepository.findById(id).get()
    val savedBuilding = savedCourt.buildings[0]
    val savedContact = savedBuilding.contacts[0]

    with(savedCourt) {
      assertThat(id).isEqualTo("SHFCRT")
      assertThat(courtName).isEqualTo("Sheffield Court")
      assertThat(courtType).isEqualTo(crownCourtType)
      assertThat(active).isEqualTo(true)
      assertThat(savedBuilding.buildingName).isEqualTo("Annex")
      assertThat(savedContact.detail).isEqualTo("55512121")
    }

    val makeACopyOfCourt = savedCourt.copy()
    val makeACopyOfBuilding = savedBuilding.copy()
    val makeACopyOfContact = savedContact.copy()

    savedCourt.courtDescription = "New Description"
    savedBuilding.buildingName = "Changed"
    savedContact.detail = "5556666"

    TestTransaction.flagForCommit()
    TestTransaction.end()

    TestTransaction.start()
    val updatedCourt = courtRepository.findById(id).get()

    with(updatedCourt) {
      val updatedBuilding = buildings[0]
      val updatedContact = updatedBuilding.contacts[0]

      assertThat(courtDescription).isEqualTo("New Description")
      assertThat(updatedBuilding.buildingName).isEqualTo("Changed")
      assertThat(updatedContact.detail).isEqualTo("5556666")

      assertThat(createdDatetime).isEqualTo(makeACopyOfCourt.createdDatetime)
      assertThat(updatedBuilding.createdDatetime).isEqualTo(makeACopyOfBuilding.createdDatetime)
      assertThat(updatedContact.createdDatetime).isEqualTo(makeACopyOfContact.createdDatetime)

      assertThat(lastUpdatedDatetime).isNotEqualTo(makeACopyOfCourt.lastUpdatedDatetime)
      assertThat(updatedBuilding.lastUpdatedDatetime).isNotEqualTo(makeACopyOfBuilding.lastUpdatedDatetime)
      assertThat(updatedContact.lastUpdatedDatetime).isNotEqualTo(makeACopyOfContact.lastUpdatedDatetime)
    }
  }

  // This finds JPA query bugs where the count query does not correspond with the main query - which often occurs when using `select distinct`
  @Test
  fun `all pages should agree on the total number of elements`() {
    fun getPage(pageNumber: Int) = courtRepository.findPageWithTextSearch(
      null, null, "crown court",
      PageRequest.of(pageNumber, 40, Sort.by("courtName"))
    )

    var pageNumber = 0
    val uniqueTotalElementValues = mutableSetOf<Long>()
    do {
      val nextPage = getPage(pageNumber++)
      uniqueTotalElementValues += nextPage.totalElements
    } while (nextPage.isLast.not())

    assertThat(uniqueTotalElementValues.size).isEqualTo(1)
  }
}
