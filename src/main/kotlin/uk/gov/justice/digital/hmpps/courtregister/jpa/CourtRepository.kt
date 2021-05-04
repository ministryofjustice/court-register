package uk.gov.justice.digital.hmpps.courtregister.jpa

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.data.repository.query.Param
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

  @Query(
    """
    select distinct c from Court c 
    where (:active is null or c.active = :active) 
    and (coalesce(:courtTypeIds) is null or c.courtType.id in (:courtTypeIds))
  """
  )
  fun findPage(
    @Param("active") active: Boolean?,
    @Param("courtTypeIds") courtTypeId: List<String>?,
    pageable: Pageable
  ): Page<Court>

  // Note that the `(:textSearch is null or (search_court_text(:textSearch) = true)` trick doesn't work for an SQLTemplateFunction
  // So we use a separate query if a text search has been included
  @Query(
    """
    select distinct c from Court c 
    join CourtTextSearch ts on c.id = ts.id
    where (:active is null or c.active = :active) 
    and (coalesce(:courtTypeIds) is null or c.courtType.id in (:courtTypeIds))
    and (search_court_text(:textSearch) = true)
  """
  )
  fun findPageWithTextSearch(
    @Param("active") active: Boolean?,
    @Param("courtTypeIds") courtTypeId: List<String>?,
    @Param("textSearch") textSearch: String,
    pageable: Pageable
  ): Page<Court>
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
