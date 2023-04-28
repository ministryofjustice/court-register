package uk.gov.justice.digital.hmpps.courtregister.service

import jakarta.persistence.EntityNotFoundException
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.courtregister.jpa.BuildingRepository
import uk.gov.justice.digital.hmpps.courtregister.jpa.Contact
import uk.gov.justice.digital.hmpps.courtregister.jpa.ContactRepository
import uk.gov.justice.digital.hmpps.courtregister.resource.ContactDto
import uk.gov.justice.digital.hmpps.courtregister.resource.UpdateContactDto

@Service
@Transactional
class BuildingContactService(
  private val contactRepository: ContactRepository,
  private val buildingRepository: BuildingRepository,
) {
  fun findById(courtId: String, buildingId: Long, contactId: Long): ContactDto {
    return ContactDto(getContact(courtId, buildingId, contactId))
  }

  fun updateContact(courtId: String, buildingId: Long, contactId: Long, updateContactRecord: UpdateContactDto): ContactDto {
    val contact = getContact(courtId, buildingId, contactId)

    with(contact) {
      type = updateContactRecord.type
      detail = updateContactRecord.detail
    }
    return ContactDto(contact)
  }

  fun insertContact(courtId: String, buildingId: Long, updateContactRecord: UpdateContactDto): ContactDto {
    val building = buildingRepository.findById(buildingId)
      .orElseThrow { EntityNotFoundException("Building Id $buildingId not found") }

    if (courtId != building.court.id) {
      throw EntityNotFoundException("Building $buildingId not in court $courtId")
    }

    val newContact = Contact(
      type = updateContactRecord.type,
      detail = updateContactRecord.detail,
      building = building,
    )

    return ContactDto(contactRepository.save(newContact))
  }

  fun deleteContact(courtId: String, buildingId: Long, contactId: Long) {
    val contact = getContact(courtId, buildingId, contactId)

    contactRepository.delete(contact)
  }

  private fun getContact(
    courtId: String,
    buildingId: Long,
    contactId: Long,
  ): Contact {
    val contact = contactRepository.findById(contactId)
      .orElseThrow { EntityNotFoundException("Contact $contactId not found") }

    if (buildingId != contact.building.id) {
      throw EntityNotFoundException("Contact $contactId not in building $buildingId")
    }

    if (courtId != contact.building.court.id) {
      throw EntityNotFoundException("Contact $contactId not in court $courtId")
    }
    return contact
  }
}
