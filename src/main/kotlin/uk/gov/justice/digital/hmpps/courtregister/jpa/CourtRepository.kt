package uk.gov.justice.digital.hmpps.courtregister.jpa

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import javax.persistence.Entity
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
  var courtType: String = "Other",
  var active: Boolean
)
