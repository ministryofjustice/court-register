package uk.gov.justice.digital.hmpps.courtregister.jpa

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import javax.persistence.Entity
import javax.persistence.Id

@Repository
interface CourtTypeRepository : CrudRepository<CourtType, String>

@Entity
data class CourtType(
  @Id
  val id: String,
  var description: String,
)
