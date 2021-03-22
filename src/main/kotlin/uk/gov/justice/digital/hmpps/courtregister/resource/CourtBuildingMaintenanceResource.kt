package uk.gov.justice.digital.hmpps.courtregister.resource

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.courtregister.ErrorResponse
import uk.gov.justice.digital.hmpps.courtregister.service.AuditService
import uk.gov.justice.digital.hmpps.courtregister.service.CourtBuildingService
import uk.gov.justice.digital.hmpps.courtregister.service.EventType
import uk.gov.justice.digital.hmpps.courtregister.service.EventType.COURT_REGISTER_BUILDING_UPDATE
import uk.gov.justice.digital.hmpps.courtregister.service.SnsService
import javax.validation.Valid
import javax.validation.constraints.Size

@RestController
@Validated
@RequestMapping(name = "Court Maintenance", path = ["/court-maintenance"], produces = [MediaType.APPLICATION_JSON_VALUE])
class CourtBuildingMaintenanceResource(
  private val buildingService: CourtBuildingService,
  private val snsService: SnsService,
  private val auditService: AuditService,
) {
  @PreAuthorize("hasRole('ROLE_MAINTAIN_REF_DATA') and hasAuthority('SCOPE_write')")
  @Operation(
    summary = "Update specified building details",
    description = "Updates building information, role required is MAINTAIN_REF_DATA",
    security = [SecurityRequirement(name = "MAINTAIN_REF_DATA", scopes = ["write"])],
    requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(
      content = [
        Content(
          mediaType = "application/json",
          schema = Schema(implementation = UpdateBuildingDto::class)
        )
      ]
    ),
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Building Information Updated",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = BuildingDto::class))]
      ),
      ApiResponse(
        responseCode = "400",
        description = "Information request to update building",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]
      ),
      ApiResponse(
        responseCode = "401",
        description = "Unauthorized to access this endpoint",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]
      ),
      ApiResponse(
        responseCode = "403",
        description = "Incorrect permissions to make building update",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]
      ),
      ApiResponse(
        responseCode = "404",
        description = "Building ID not found",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]
      )
    ]
  )
  @PutMapping("/id/{courtId}/buildings/{buildingId}")
  fun updateBuilding(
    @Schema(description = "Court ID", example = "ACCRYC", required = true)
    @PathVariable @Size(max = 12, min = 2, message = "Court ID must be between 2 and 12") courtId: String,
    @Schema(description = "Building ID", example = "234231", required = true)
    @PathVariable buildingId: Long,
    @RequestBody @Valid updateBuildingDto: UpdateBuildingDto
  ): BuildingDto {
    val updatedBuilding = buildingService.updateBuilding(courtId, buildingId, updateBuildingDto)
    snsService.sendEvent(COURT_REGISTER_BUILDING_UPDATE, courtId)
    auditService.sendAuditEvent(
      COURT_REGISTER_BUILDING_UPDATE.name,
      mapOf("courtId" to courtId, "buildingId" to buildingId, "building" to updatedBuilding)
    )
    return updatedBuilding
  }

  @PreAuthorize("hasRole('ROLE_MAINTAIN_REF_DATA') and hasAuthority('SCOPE_write')")
  @PostMapping("/id/{courtId}/buildings")
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(
    summary = "Add a new building to a court",
    description = "Adds a new building to court, role required is MAINTAIN_REF_DATA",
    security = [SecurityRequirement(name = "MAINTAIN_REF_DATA", scopes = ["write"])],
    requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(
      content = [
        Content(
          mediaType = "application/json",
          schema = Schema(implementation = UpdateBuildingDto::class)
        )
      ]
    ),
    responses = [
      ApiResponse(
        responseCode = "201",
        description = "Building Information Inserted",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = BuildingDto::class)
          )
        ]
      ),
      ApiResponse(
        responseCode = "400",
        description = "Invalid request to add a building",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class)
          )
        ]
      ),
      ApiResponse(
        responseCode = "401",
        description = "Unauthorized to access this endpoint",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]
      ),
      ApiResponse(
        responseCode = "403",
        description = "Incorrect permissions to make building insert",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]
      )
    ]
  )
  fun insertBuilding(
    @Schema(description = "Court ID", example = "ACCRYC", required = true)
    @PathVariable @Size(max = 12, min = 2, message = "Court ID must be between 2 and 12") courtId: String,
    @RequestBody @Valid updateBuildingDto: UpdateBuildingDto
  ): BuildingDto {
    val newBuilding = buildingService.insertBuilding(courtId, updateBuildingDto)
    snsService.sendEvent(EventType.COURT_REGISTER_BUILDING_INSERT, courtId)
    auditService.sendAuditEvent(
      EventType.COURT_REGISTER_BUILDING_INSERT.name,
      mapOf("courtId" to courtId, "building" to newBuilding)
    )

    return newBuilding
  }

  @PreAuthorize("hasRole('ROLE_MAINTAIN_REF_DATA') and hasAuthority('SCOPE_write')")
  @Operation(
    summary = "Delete specified building",
    description = "Deletes building information, role required is MAINTAIN_REF_DATA",
    security = [SecurityRequirement(name = "MAINTAIN_REF_DATA", scopes = ["write"])],
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Building Information Deleted",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = BuildingDto::class))]
      ),
      ApiResponse(
        responseCode = "401",
        description = "Unauthorized to access this endpoint",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]
      ),
      ApiResponse(
        responseCode = "403",
        description = "Incorrect permissions to delete building",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]
      ),
      ApiResponse(
        responseCode = "404",
        description = "Building ID not found",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]
      )
    ]
  )
  @DeleteMapping("/id/{courtId}/buildings/{buildingId}")
  fun deleteBuilding(
    @Schema(description = "Court ID", example = "ACCRYC", required = true)
    @PathVariable @Size(max = 12, min = 2, message = "Court ID must be between 2 and 12") courtId: String,
    @Schema(description = "Building ID", example = "234231", required = true)
    @PathVariable buildingId: Long
  ) {
    buildingService.deleteBuilding(courtId, buildingId)
    snsService.sendEvent(EventType.COURT_REGISTER_BUILDING_DELETE, courtId)
    auditService.sendAuditEvent(
      EventType.COURT_REGISTER_BUILDING_DELETE.name,
      mapOf("courtId" to courtId, "buildingId" to buildingId)
    )
  }
}

@JsonInclude(NON_NULL)
@Schema(description = "Building Update Record")
data class UpdateBuildingDto(
  @Schema(description = "Building Name", example = "Crown House") val buildingName: String?,
  @Schema(description = "Street Number and Name", example = "452 West Street") val street: String?,
  @Schema(description = "Locality", example = "West Cross") val locality: String?,
  @Schema(description = "Town/City", example = "Swansea") val town: String?,
  @Schema(description = "County", example = "South Glamorgan") val county: String?,
  @Schema(description = "Postcode", example = "SA3 4HT") val postcode: String?,
  @Schema(description = "Country", example = "UK") val country: String?,
  @Schema(description = "Sub location code for referencing building", example = "AAABBB") val subCode: String?

)
