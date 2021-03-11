package uk.gov.justice.digital.hmpps.courtregister.jpa

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import javax.persistence.CascadeType
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.OneToMany

@Repository
interface BuildingRepository : CrudRepository<Building, Long>

@Entity
data class Building(
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  val id: Long? = null,
  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "COURT_CODE")
  val court: Court,
  val subCode: String?,
  val buildingName: String?,
  val street: String?,
  val locality: String?,
  val town: String?,
  val county: String?,
  val postcode: String?,
  val country: String?,
  @CreatedDate
  var createdDatetime: LocalDateTime = LocalDateTime.now(),
  @LastModifiedDate
  var lastUpdatedDatetime: LocalDateTime = LocalDateTime.now(),

  @OneToMany(cascade = [CascadeType.ALL], mappedBy = "building")
  var contacts: List<Contact> = listOf()

)
