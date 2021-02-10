package uk.gov.justice.digital.hmpps.courtregister.resource

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.courtregister.ErrorResponse
import uk.gov.justice.digital.hmpps.courtregister.service.CourtService
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

@RestController
@Validated
@RequestMapping("/court-maintenance", produces = [MediaType.APPLICATION_JSON_VALUE])
class CourtMaintenanceResource(private val courtService: CourtService) {
  @PreAuthorize("hasRole('ROLE_MAINTAIN_REF_DATA')")
  @Operation(
    summary = "Update specified court details",
    description = "Updates court information, role required is MAINTAIN_REF_DATA",
    security = [ SecurityRequirement(name = "MAINTAIN_REF_DATA", scopes = ["write"])],
    requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(content = [Content(mediaType = "application/json", schema = Schema(implementation = UpdateCourtDto::class))]),
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Court Information Updated",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = CourtDto::class))]
      ),
      ApiResponse(
        responseCode = "400",
        description = "Information request to update court",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]
      ),
      ApiResponse(
        responseCode = "403",
        description = "Incorrect permissions to make court update"
      ),
      ApiResponse(
        responseCode = "404",
        description = "Court ID not found",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]
      )
    ]
  )
  @PutMapping("/id/{courtId}")
  fun updateCourt(
    @Schema(description = "Court ID", example = "ACCRYC", required = true)
    @PathVariable @Size(max = 12, min = 2, message = "Court ID must be between 2 and 12") courtId: String,
    @RequestBody courtUpdateRecord: UpdateCourtDto
  ): CourtDto =
    courtService.updateCourt(courtId, courtUpdateRecord)

  @PreAuthorize("hasRole('ROLE_MAINTAIN_REF_DATA')")
  @PostMapping("")
  @Operation(
    summary = "Add a new court",
    description = "Adds a new court information, role required is MAINTAIN_REF_DATA",
    security = [ SecurityRequirement(name = "MAINTAIN_REF_DATA", scopes = ["write"])],
    requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(content = [Content(mediaType = "application/json", schema = Schema(implementation = CourtDto::class))]),
    responses = [
      ApiResponse(
        responseCode = "201",
        description = "Court Information Inserted",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = CourtDto::class)
          )
        ]
      ),
      ApiResponse(
        responseCode = "400",
        description = "Information request to add a court",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class)
          )
        ]
      ),
      ApiResponse(
        responseCode = "403",
        description = "Incorrect permissions to make court insert"
      )
    ]
  )
  fun insertCourt(
    @RequestBody courtInsertRecord: CourtDto
  ): CourtDto =
    courtService.insertCourt(courtInsertRecord)
}

@JsonInclude(NON_NULL)
@Schema(description = "Court Update Record")
data class UpdateCourtDto(
  @Schema(description = "Name of the court", example = "Accrington Youth Court", required = true) @Size(max = 80, min = 2, message = "Court name must be between 2 and 80") @NotBlank val courtName: String,
  @Schema(description = "Description of the court", example = "Accrington Youth Court", required = false) @Size(max = 200, min = 2, message = "Court name must be between 2 and 200") val courtDescription: String?,
  @Schema(description = "Type of court", example = "Crown Court", required = true, allowableValues = ["Magistrates Court", "Youth Court", "Crown Court", "Other"]) @Size(max = 40, min = 2, message = "Court Type must be between 2 and 40") val courtType: String,
  @Schema(description = "Whether the court is still active", required = true) val active: Boolean
)
