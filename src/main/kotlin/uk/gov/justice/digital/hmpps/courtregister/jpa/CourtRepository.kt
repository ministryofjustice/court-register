package uk.gov.justice.digital.hmpps.courtregister.jpa

import com.vladmihalcea.hibernate.type.search.PostgreSQLTSVectorType
import org.hibernate.annotations.Immutable
import org.hibernate.boot.MetadataBuilder
import org.hibernate.boot.spi.MetadataBuilderContributor
import org.hibernate.dialect.function.SQLFunctionTemplate
import org.hibernate.type.BooleanType
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
import javax.persistence.Table

@Repository
interface CourtRepository : PagingAndSortingRepository<Court, String> {

  fun findByActiveOrderById(active: Boolean): List<Court>

  @Query(
    """
    select c from Court c 
    where (:active is null or c.active = :active) 
    and (coalesce(:courtTypeIds) is null or c.courtType.id in (:courtTypeIds))
  """
  )
  fun findPage(
    @Param("active") active: Boolean?,
    @Param("courtTypeIds") courtTypeId: List<String>?,
    pageable: Pageable
  ): Page<Court>

  // TODO DT-1954 the `:textSearch is null or (fts(:textSearch) = true)` trick doesn't work for an SQLTemplateFunction - use separate methods for now and try to fix later
  @Query(
    """
    select c from Court c 
    join TextSearch ts on c.id = ts.id
    where (:active is null or c.active = :active) 
    and (coalesce(:courtTypeIds) is null or c.courtType.id in (:courtTypeIds))
    and (fts(:textSearch) = true)
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

@Entity
@Immutable
@Table(name = "`text_search`")
data class TextSearch(
  @Id
  val id: String,
  val tsv: PostgreSQLTSVectorType,
)

class TextSearchSqlFunctionTemplate : MetadataBuilderContributor {
  override fun contribute(metadataBuilder: MetadataBuilder) {
    metadataBuilder.applySqlFunction("fts", SQLFunctionTemplate(BooleanType.INSTANCE, "tsv @@ plainto_tsquery(?1)"))
  }
}
