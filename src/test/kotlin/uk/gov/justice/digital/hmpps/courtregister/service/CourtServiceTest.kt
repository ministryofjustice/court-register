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
import uk.gov.justice.digital.hmpps.courtregister.jpa.CourtRepository
import uk.gov.justice.digital.hmpps.courtregister.resource.CourtDto
import uk.gov.justice.digital.hmpps.courtregister.resource.UpdateCourtDto
import java.util.Optional
import javax.persistence.EntityExistsException
import javax.persistence.EntityNotFoundException

class CourtServiceTest {
  private val courtRepository: CourtRepository = mock()
  private val courtService = CourtService(courtRepository)

  @Suppress("ClassName")
  @Nested
  inner class findById {
    @Test
    fun `find court`() {
      whenever(courtRepository.findById(anyString())).thenReturn(
        Optional.of(Court("ACCRYC", "A Court", null, "Crown", true))
      )
      val courtDto = courtService.findById("ACCRYC")
      assertThat(courtDto).isEqualTo(CourtDto("ACCRYC", "A Court", null, "Crown", true))
      verify(courtRepository).findById("ACCRYC")
    }
  }

  @Suppress("ClassName")
  @Nested
  inner class findCourts {
    @Test
    fun `find all active courts`() {
      val listOfCourts = listOf(
        Court("ACCRYC", "A Court 1", null, "Crown", true),
        Court("ACCRYV", "A Court 2", null, "County", true),
        Court("ACCRYT", "A Court 3", null, "Other", true)
      )
      whenever(courtRepository.findByActiveOrderById(true)).thenReturn(
        listOfCourts
      )
      val courts = courtService.findAll(true)
      assertThat(courts).isEqualTo(
        listOf(
          CourtDto("ACCRYC", "A Court 1", null, "Crown", true),
          CourtDto("ACCRYV", "A Court 2", null, "County", true),
          CourtDto("ACCRYT", "A Court 3", null, "Other", true)
        )
      )
      verify(courtRepository).findByActiveOrderById(true)
    }
  }

  @Suppress("ClassName")
  @Nested
  inner class maintainCourts {
    @Test
    fun `update a court`() {
      whenever(courtRepository.findById("ACCRYC")).thenReturn(
        Optional.of(Court("ACCRYC", "A Court 1", null, "Crown", true))
      )
      val updatedCourt =
        courtService.updateCourt("ACCRYC", UpdateCourtDto("A Court 1", "add description", "Crown", true))
      assertThat(updatedCourt).isEqualTo(
        CourtDto("ACCRYC", "A Court 1", "add description", "Crown", true)
      )
      verify(courtRepository).findById("ACCRYC")
    }

    @Test
    fun `try to update a court that doesn't exist`() {
      whenever(courtRepository.findById("ACCRYC")).thenReturn(
        Optional.empty()
      )
      Assertions.assertThrows(EntityNotFoundException::class.java) {
        courtService.updateCourt("ACCRYC", UpdateCourtDto("A Court 1", "add description", "Crown", true))
      }

      verify(courtRepository).findById("ACCRYC")
    }

    @Test
    fun `create a court`() {
      whenever(courtRepository.findById("ACCRYZ")).thenReturn(
        Optional.empty()
      )
      val courtToSave = Court("ACCRYZ", "A Court 4", "new court", "Crown", true)
      whenever(courtRepository.save(courtToSave)).thenReturn(
        courtToSave
      )
      val courtInsertRecord = CourtDto("ACCRYZ", "A Court 4", "new court", "Crown", true)
      val updatedCourt = courtService.insertCourt(courtInsertRecord)
      assertThat(updatedCourt).isEqualTo(
        courtInsertRecord
      )
      verify(courtRepository).findById("ACCRYZ")
      verify(courtRepository).save(courtToSave)
    }

    @Test
    fun `try to create a court that already exists`() {
      whenever(courtRepository.findById("ACCRYZ")).thenReturn(
        Optional.of(Court("ACCRYZ", "A Court 5", "new court 5", "Crown", false))
      )
      val courtInsertRecord = CourtDto("ACCRYZ", "A Court 4", "new court", "Crown", true)
      Assertions.assertThrows(EntityExistsException::class.java) { courtService.insertCourt(courtInsertRecord) }

      verify(courtRepository).findById("ACCRYZ")
    }
  }
}
