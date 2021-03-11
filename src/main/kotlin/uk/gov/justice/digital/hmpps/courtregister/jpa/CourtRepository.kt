package uk.gov.justice.digital.hmpps.courtregister.jpa

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import javax.persistence.CascadeType
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.OneToMany

@Repository
interface CourtDetailRepository : CrudRepository<Court, String> {
  fun findByActiveOrderById(active: Boolean): List<Court>
}

@Entity
data class Court(
  @Id
  val id: String,
  var courtName: String,
  var courtDescription: String?,
  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "TYPE", nullable = false)
  var courtType: CourtType,
  var active: Boolean,
  @CreatedDate
  var createdDatetime: LocalDateTime = LocalDateTime.now(),
  @LastModifiedDate
  var lastUpdatedDatetime: LocalDateTime = LocalDateTime.now(),

  @OneToMany(cascade = [CascadeType.ALL], mappedBy = "court")
  var buildings: List<Building> = listOf()
)
