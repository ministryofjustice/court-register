package uk.gov.justice.digital.hmpps.courtregister.jpa

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.ManyToOne

@Repository
interface ContactRepository : CrudRepository<Contact, Long>

@Entity
data class Contact(
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  val id: Long,
  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  val building: Building,
  val type: String,
  val detail: String?,
  @CreatedDate
  var createdDatetime: LocalDateTime = LocalDateTime.now(),
  @LastModifiedDate
  var lastUpdatedDatetime: LocalDateTime = LocalDateTime.now()
)
