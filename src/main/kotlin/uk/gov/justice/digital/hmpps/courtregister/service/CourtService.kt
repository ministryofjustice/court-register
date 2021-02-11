package uk.gov.justice.digital.hmpps.courtregister.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.courtregister.jpa.Court
import uk.gov.justice.digital.hmpps.courtregister.jpa.CourtRepository
import uk.gov.justice.digital.hmpps.courtregister.resource.CourtDto
import uk.gov.justice.digital.hmpps.courtregister.resource.UpdateCourtDto
import javax.persistence.EntityExistsException
import javax.persistence.EntityNotFoundException
import javax.transaction.Transactional

@Service
@Transactional
class CourtService(private val courtRepository: CourtRepository) {
  fun findById(courtId: String): CourtDto {
    val court = courtRepository.findById(courtId)
      .orElseThrow { EntityNotFoundException("Court $courtId not found") }
    return CourtDto(court)
  }

  fun findAll(activeOnly: Boolean = false): List<CourtDto> {
    if (activeOnly) {
      return courtRepository.findByActiveOrderById(true).map { CourtDto(it) }
    }
    return courtRepository.findAll().map { CourtDto(it) }
  }

  fun updateCourt(courtId: String, courtUpdateRecord: UpdateCourtDto): CourtDto {
    val court = courtRepository.findById(courtId)
      .orElseThrow { EntityNotFoundException("Court $courtId not found") }

    with(courtUpdateRecord) {
      court.courtName = courtName
      court.courtDescription = courtDescription
      court.courtType = courtType
      court.active = active
    }
    return CourtDto(court)
  }

  fun insertCourt(courtInsertRecord: CourtDto): CourtDto {
    if (courtRepository.findById(courtInsertRecord.courtId).isPresent) {
      throw EntityExistsException("Court $courtInsertRecord.courtId already exists")
    }

    with(courtInsertRecord) {
      val court = Court(courtId, courtName, courtDescription, courtType, active)
      courtRepository.save(court)
      return CourtDto(court)
    }
  }
}
