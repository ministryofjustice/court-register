package uk.gov.justice.digital.hmpps.courtregister.jpa

import jakarta.persistence.CascadeType
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.courtregister.resource.UpdateContactDto
import java.time.LocalDateTime
import java.util.Optional

@Repository
interface BuildingRepository : CrudRepository<Building, Long> {
  fun findBySubCode(subCode: String): Optional<Building>

  @Query(
    """
    select b from Building b where b.court.id = :courtId and b.subCode is null
  """,
  )
  fun findMainBuilding(courtId: String): Optional<Building>
}

@Entity
@EntityListeners(AuditingEntityListener::class)
data class Building(
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  val id: Long? = null,

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "COURT_CODE")
  var court: Court,

  var subCode: String?,
  var buildingName: String?,
  var street: String?,
  var locality: String?,
  var town: String?,
  var county: String?,
  var postcode: String?,
  var country: String?,
  var active: Boolean,

  @CreatedDate
  var createdDatetime: LocalDateTime = LocalDateTime.MIN,
  @LastModifiedDate
  var lastUpdatedDatetime: LocalDateTime = LocalDateTime.MIN,

  @OneToMany(cascade = [CascadeType.ALL], mappedBy = "building", orphanRemoval = true)
  var contacts: List<Contact> = listOf(),

) {
  fun addContact(dto: UpdateContactDto): Contact {
    val contact = Contact(building = this, type = dto.type, detail = dto.detail)
    contacts = contacts.plus(contact)
    return contact
  }
}
