package uk.gov.justice.digital.hmpps.courtregister.jpa

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Repository
interface TypeOfCourtRepository : CrudRepository<TypeOfCourt, String>

@Entity
@Table(name = "COURT_TYPE")
data class TypeOfCourt(
  @Id
  val id: String,
  var description: String
)
