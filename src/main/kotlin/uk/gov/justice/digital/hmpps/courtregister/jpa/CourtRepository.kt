package uk.gov.justice.digital.hmpps.courtregister.jpa

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EntityListeners
import javax.persistence.FetchType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.OneToMany

@Repository
interface CourtRepository : PagingAndSortingRepository<Court, String> {
  fun findByActiveOrderById(active: Boolean): List<Court>
  fun findByActive(active: Boolean, pageable: Pageable): Page<Court>
  fun findByCourtType(courtType: CourtType, pageable: Pageable): Page<Court>
  fun findByActiveAndCourtType(active: Boolean, courtType: CourtType, pageable: Pageable): Page<Court>
}

@Entity
@EntityListeners(AuditingEntityListener::class)
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
  @Column(nullable = false)
  var createdDatetime: LocalDateTime = LocalDateTime.MIN,

  @LastModifiedDate
  @Column(nullable = false)
  var lastUpdatedDatetime: LocalDateTime = LocalDateTime.MIN,

  @OneToMany(cascade = [CascadeType.ALL], mappedBy = "court", orphanRemoval = true)
  val buildings: MutableList<Building>? = mutableListOf()
)
