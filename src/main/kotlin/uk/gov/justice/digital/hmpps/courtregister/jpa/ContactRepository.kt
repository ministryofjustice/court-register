package uk.gov.justice.digital.hmpps.courtregister.jpa

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EntityListeners
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.ManyToOne

@Repository
interface ContactRepository : CrudRepository<Contact, Long>

@Entity
@EntityListeners(AuditingEntityListener::class)
data class Contact(
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  val id: Long? = null,

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  var building: Building,

  var type: String,
  var detail: String?,

  @CreatedDate
  @Column(nullable = false)
  var createdDatetime: LocalDateTime = LocalDateTime.MIN,
  @LastModifiedDate
  @Column(nullable = false)
  var lastUpdatedDatetime: LocalDateTime = LocalDateTime.MIN,
)
