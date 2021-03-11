package uk.gov.justice.digital.hmpps.courtregister.resource

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.springframework.http.MediaType
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.courtregister.ErrorResponse
import uk.gov.justice.digital.hmpps.courtregister.jpa.Court
import uk.gov.justice.digital.hmpps.courtregister.jpa.CourtType
import uk.gov.justice.digital.hmpps.courtregister.service.CourtService
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

@RestController
@Validated
@RequestMapping("/courts", produces = [MediaType.APPLICATION_JSON_VALUE])
class CourtResource(private val courtService: CourtService) {
  @GetMapping("/id/{courtId}")
  @Operation(
    summary = "Get specified court",
    description = "Information on a specific court",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Court Information Returned",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = CourtDto::class))]
      ),
      ApiResponse(
        responseCode = "400",
        description = "Incorrect request to get court information",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]
      ),
      ApiResponse(
        responseCode = "404",
        description = "Court ID not found",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]
      )
    ]
  )
  fun getCourtFromId(
    @Schema(description = "Court ID", example = "ACCRYC", required = true)
    @PathVariable @Size(max = 12, min = 2, message = "Court ID must be between 2 and 12") courtId: String
  ): CourtDto =
    courtService.findById(courtId)

  @GetMapping("")
  @Operation(
    summary = "Get all active courts",
    description = "All courts (active only)",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "All Active Court Information Returned",
        content = arrayOf(
          Content(
            mediaType = "application/json",
            array = ArraySchema(schema = Schema(implementation = CourtDto::class))
          )
        )
      )
    ]
  )
  fun getActiveCourts(): List<CourtDto> =
    courtService.findAll(true)

  @GetMapping("/types")
  @Operation(
    summary = "Get all types of court",
    description = "All court types",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "All Types of courts returned",
        content = arrayOf(
          Content(
            mediaType = "application/json",
            array = ArraySchema(schema = Schema(implementation = CourtTypeDto::class))
          )
        )
      )
    ]
  )
  fun getCourtTypes(): List<CourtTypeDto> =
    courtService.getCourtTypes()

  @GetMapping("/all")
  @Operation(
    summary = "Get all active and inactive courts",
    description = "All active/inactive courts",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "All Court Information Returned (Active only)",
        content = arrayOf(
          Content(
            mediaType = "application/json",
            array = ArraySchema(schema = Schema(implementation = CourtDto::class))
          )
        )
      )
    ]
  )
  fun getAllCourts(): List<CourtDto> =
    courtService.findAll(false)
}

@JsonInclude(NON_NULL)
@Schema(description = "Court Information")
data class CourtDto(
  @Schema(description = "Court ID", example = "ACCRYC", required = true) @field:Size(max = 12, min = 2, message = "Court ID must be between 2 and 12") @NotBlank val courtId: String,
  @Schema(description = "Name of the court", example = "Accrington Youth Court", required = true) @field:Size(max = 80, min = 2, message = "Court name must be between 2 and 80") @NotBlank val courtName: String,
  @Schema(description = "Description of the court", example = "Accrington Youth Court", required = false) @field:Size(max = 200, min = 2, message = "Court name must be between 2 and 200") val courtDescription: String?,
  @Schema(description = "Type of court", example = "CROWN", required = true) val courtType: String,
  @Schema(description = "Whether the court is still active", required = true) val active: Boolean
) {
  constructor(court: Court) : this(court.id, court.courtName, court.courtDescription, court.courtType.id, court.active)
}

@JsonInclude(NON_NULL)
@Schema(description = "Court Type")
data class CourtTypeDto(
  @Schema(description = "Type of court", example = "CROWN", required = true) val courtType: String,
  @Schema(description = "Description of the type of court", example = "Crown Court", required = true) @NotBlank val courtName: String
) {
  constructor(courtType: CourtType) : this(courtType.id, courtType.description)
}
