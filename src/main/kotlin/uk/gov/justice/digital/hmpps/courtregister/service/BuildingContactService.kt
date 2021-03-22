package uk.gov.justice.digital.hmpps.courtregister.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.courtregister.jpa.BuildingRepository
import uk.gov.justice.digital.hmpps.courtregister.jpa.Contact
import uk.gov.justice.digital.hmpps.courtregister.jpa.ContactRepository
import uk.gov.justice.digital.hmpps.courtregister.resource.ContactDto
import uk.gov.justice.digital.hmpps.courtregister.resource.UpdateContactDto
import javax.persistence.EntityNotFoundException
import javax.transaction.Transactional

@Service
@Transactional
class BuildingContactService(
  private val contactRepository: ContactRepository,
  private val buildingRepository: BuildingRepository
) {
  fun findById(courtId: String, buildingId: Long, contactId: Long): ContactDto {
    return ContactDto(getContact(contactId, buildingId, courtId))
  }

  fun updateContact(courtId: String, buildingId: Long, contactId: Long, updateContactRecord: UpdateContactDto): ContactDto {
    val contact = getContact(contactId, buildingId, courtId)

    with(contact) {
      type = updateContactRecord.type
      detail = updateContactRecord.detail
    }
    return ContactDto(contact)
  }

  private fun getContact(
    contactId: Long,
    buildingId: Long,
    courtId: String
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

  fun insertContact(courtId: String, buildingId: Long, updateContactRecord: UpdateContactDto): ContactDto {

    val building = buildingRepository.findById(buildingId)
      .orElseThrow { EntityNotFoundException("Building Id $buildingId not found") }

    if (courtId != building.court.id) {
      throw EntityNotFoundException("Building $buildingId not in court $courtId")
    }

    val newContact = Contact(
      type = updateContactRecord.type,
      detail = updateContactRecord.detail,
      building = building
    )
    contactRepository.save(newContact)
    return ContactDto(newContact)
  }

  fun deleteContact(contactId: Long) {
    val contact = contactRepository.findById(contactId)
      .orElseThrow { EntityNotFoundException("Contact Id $contactId not found") }

    contactRepository.delete(contact)
  }
}
