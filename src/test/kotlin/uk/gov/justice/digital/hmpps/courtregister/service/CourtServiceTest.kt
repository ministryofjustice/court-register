package uk.gov.justice.digital.hmpps.courtregister.service

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import uk.gov.justice.digital.hmpps.courtregister.jpa.Court
import uk.gov.justice.digital.hmpps.courtregister.jpa.CourtDetailRepository
import uk.gov.justice.digital.hmpps.courtregister.jpa.CourtType
import uk.gov.justice.digital.hmpps.courtregister.jpa.CourtTypeRepository
import uk.gov.justice.digital.hmpps.courtregister.resource.CourtDto
import uk.gov.justice.digital.hmpps.courtregister.resource.UpdateCourtDto
import java.time.LocalDateTime.now
import java.util.Optional
import javax.persistence.EntityExistsException
import javax.persistence.EntityNotFoundException

class CourtServiceTest {
  private val courtRepository: CourtDetailRepository = mock()
  private val courtTypeRepository: CourtTypeRepository = mock()
  private val courtService = CourtService(courtRepository, courtTypeRepository)

  @Suppress("ClassName")
  @Nested
  inner class findById {
    @Test
    fun `find court`() {
      whenever(courtRepository.findById(anyString())).thenReturn(
        Optional.of(Court("ACCRYC", "A Court", null, CourtType("CROWN", "Crown Court"), true))
      )
      val courtDto = courtService.findById("ACCRYC")
      assertThat(courtDto).isEqualTo(CourtDto("ACCRYC", "A Court", null, "CROWN", true))
      verify(courtRepository).findById("ACCRYC")
    }
  }

  @Suppress("ClassName")
  @Nested
  inner class findCourts {
    @Test
    fun `find all active courts`() {
      val listOfCourts = listOf(
        Court("ACCRYC", "A Court 1", null, CourtType("CROWN", "Crown Court"), true),
        Court("ACCRYV", "A Court 2", null, CourtType("COUNTY", "County Court"), true),
        Court("ACCRYT", "A Court 3", null, CourtType("OTHER", "Other Type Court"), true)
      )
      whenever(courtRepository.findByActiveOrderById(true)).thenReturn(
        listOfCourts
      )
      val courts = courtService.findAll(true)
      assertThat(courts).isEqualTo(
        listOf(
          CourtDto("ACCRYC", "A Court 1", null, "CROWN", true),
          CourtDto("ACCRYV", "A Court 2", null, "COUNTY", true),
          CourtDto("ACCRYT", "A Court 3", null, "OTHER", true)
        )
      )
      verify(courtRepository).findByActiveOrderById(true)
    }

    @Test
    fun `find all courts`() {
      val listOfCourts = listOf(
        Court("ACCRYC", "A Court 1", null, CourtType("CROWN", "Crown Court"), true),
        Court("ACCRYV", "A Court 2", null, CourtType("COUNTY", "County Court"), true),
        Court("ACCRYT", "A Court 3", null, CourtType("OTHER", "Other Type Court"), false)
      )
      whenever(courtRepository.findAll()).thenReturn(
        listOfCourts
      )
      val courts = courtService.findAll()
      assertThat(courts).isEqualTo(
        listOf(
          CourtDto("ACCRYC", "A Court 1", null, "CROWN", true),
          CourtDto("ACCRYV", "A Court 2", null, "COUNTY", true),
          CourtDto("ACCRYT", "A Court 3", null, "OTHER", false)
        )
      )
      verify(courtRepository).findAll()
    }
  }

  @Suppress("ClassName")
  @Nested
  inner class maintainCourts {
    @Test
    fun `update a court`() {
      whenever(courtRepository.findById("ACCRYC")).thenReturn(
        Optional.of(Court("ACCRYC", "A Court 1", null, CourtType("CROWN", "Crown Court"), true))
      )
      whenever(courtTypeRepository.findById("CROWN")).thenReturn(Optional.of(CourtType("CROWN", "Crown Court")))
      val updatedCourt =
        courtService.updateCourt("ACCRYC", UpdateCourtDto("A Court 1", "add description", "CROWN", true))
      assertThat(updatedCourt).isEqualTo(
        CourtDto("ACCRYC", "A Court 1", "add description", "CROWN", true)
      )
      verify(courtRepository).findById("ACCRYC")
    }

    @Test
    fun `try to update a court that doesn't exist`() {
      whenever(courtRepository.findById("ACCRYC")).thenReturn(
        Optional.empty()
      )
      Assertions.assertThrows(EntityNotFoundException::class.java) {
        courtService.updateCourt("ACCRYC", UpdateCourtDto("A Court 1", "add description", "CROWN", true))
      }

      verify(courtRepository).findById("ACCRYC")
    }

    @Test
    fun `create a court`() {
      whenever(courtRepository.findById("ACCRYZ")).thenReturn(
        Optional.empty()
      )
      val createdDatetime = now()
      val courtToSave = Court("ACCRYZ", "A Court 4", "new court", CourtType("CROWN", "Crown Court"), true)
      whenever(courtRepository.save(courtToSave)).thenReturn(
        courtToSave
      )
      whenever(courtTypeRepository.findById("CROWN")).thenReturn(Optional.of(CourtType("CROWN", "Crown Court")))
      val courtInsertRecord = CourtDto("ACCRYZ", "A Court 4", "new court", "CROWN", true)
      val updatedCourt = courtService.insertCourt(courtInsertRecord)
      assertThat(updatedCourt).isEqualTo(
        courtInsertRecord
      )
      verify(courtRepository).findById("ACCRYZ")
      verify(courtTypeRepository).findById("CROWN")
    }

    @Test
    fun `try to create a court that already exists`() {
      whenever(courtRepository.findById("ACCRYZ")).thenReturn(
        Optional.of(Court("ACCRYZ", "A Court 5", "new court 5", CourtType("CROWN", "Crown Court"), false))
      )
      val courtInsertRecord = CourtDto("ACCRYZ", "A Court 4", "new court", "CROWN", true)
      Assertions.assertThrows(EntityExistsException::class.java) { courtService.insertCourt(courtInsertRecord) }

      verify(courtRepository).findById("ACCRYZ")
    }
  }
}
