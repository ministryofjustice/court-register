package uk.gov.justice.digital.hmpps.courtregister.jpa

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.courtregister.jpa.Court.CourtType.OTHER
import javax.persistence.Entity
import javax.persistence.EnumType.STRING
import javax.persistence.Enumerated
import javax.persistence.Id

@Repository
interface CourtRepository : CrudRepository<Court, String> {
  fun findByActiveOrderById(active: Boolean): List<Court>
}

@Entity
data class Court(
  @Id
  val id: String,
  var courtName: String,
  var courtDescription: String?,
  @Enumerated(STRING)
  var courtType: CourtType = OTHER,
  var active: Boolean
) {
  enum class CourtType(val label: String) {
    MAGISTRATES("Magistrates Court"),
    CROWN("Crown Court"),
    COUNTY("County Court"),
    YOUTH("Youth Court"),
    OTHER("Other"),
    SHERRIFS_SCOTTISH("Sherrifs Court (Scottish)"),
    DISTRICT_SCOTTISH("District Court (Scottish)"),
    HIGH_COURT_SCOTTISH("High Court (Scottish)"),
    ASYLUM_IMMIGRATION("Asylum and Immigration Tribunal"),
    IMMIGRATION("Immigration"),
    COURTS_MARTIAL("Courts Martial"),
    OUTSIDE_ENG_WALES("Outside England and Wales"),
    APPEAL("Court of Appeal")
  }
}
