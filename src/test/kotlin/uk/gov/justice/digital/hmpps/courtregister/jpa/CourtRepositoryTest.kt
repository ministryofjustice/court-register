package uk.gov.justice.digital.hmpps.courtregister.jpa

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.transaction.TestTransaction
import org.springframework.transaction.annotation.Transactional

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = WebEnvironment.MOCK)
@Transactional
class CourtRepositoryTest {

  @Autowired
  lateinit var courtDetailRepository: CourtDetailRepository

  @Autowired
  lateinit var courtTypeRepository: CourtTypeRepository

  @Test
  fun `should load court data`() {
    val court = courtDetailRepository.findById("ABRYMC").orElseThrow()

    with(court) {
      assertThat(id).isEqualTo("ABRYMC")
      assertThat(courtTypeType.description).isEqualTo("Magistrates Court")
      assertThat(buildings[0].buildingName).isEqualTo("Swyddfa'r Sir")
      assertThat(buildings[0].contacts[0].detail).isEqualTo("01633 645000")
    }
  }

  @Test
  fun `should insert court`() {
    val crownCourtType = courtTypeRepository.findById("CRN").orElseThrow()

    val court = Court("SHFCRT", "Sheffield Court", "A Court in Sheffield", crownCourtType, true)

    court.buildings += Building(court = court, subCode = null, street = null, buildingName = "Annex", locality = null, town = null, postcode = null, county = null, country = "UK")

    val id = courtDetailRepository.save(court).id

    TestTransaction.flagForCommit()
    TestTransaction.end()

    val savedCourt = courtDetailRepository.findById(id).get()

    with(savedCourt) {
      assertThat(id).isEqualTo("SHFCRT")
      assertThat(courtName).isEqualTo("Sheffield Court")
      assertThat(courtTypeType).isEqualTo(crownCourtType)
      assertThat(active).isEqualTo(true)
    }
  }
}
