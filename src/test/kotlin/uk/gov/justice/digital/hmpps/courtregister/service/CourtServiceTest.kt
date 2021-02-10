package uk.gov.justice.digital.hmpps.courtregister.service

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import uk.gov.justice.digital.hmpps.courtregister.jpa.Court
import uk.gov.justice.digital.hmpps.courtregister.jpa.CourtRepository
import uk.gov.justice.digital.hmpps.courtregister.resource.CourtDto
import java.util.Optional

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
  inner class findActive {
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
      assertThat(courts).isEqualTo(listOf(
        CourtDto("ACCRYC", "A Court 1", null, "Crown", true),
        CourtDto("ACCRYV", "A Court 2", null, "County", true),
        CourtDto("ACCRYT", "A Court 3", null, "Other", true)
      ))
      verify(courtRepository).findByActiveOrderById(true)
    }
  }
}
