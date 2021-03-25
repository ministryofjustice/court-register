package uk.gov.justice.digital.hmpps.courtregister.resource

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.http.MediaType
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.courtregister.ErrorResponse
import uk.gov.justice.digital.hmpps.courtregister.jpa.Building
import uk.gov.justice.digital.hmpps.courtregister.jpa.Contact
import uk.gov.justice.digital.hmpps.courtregister.jpa.Court
import uk.gov.justice.digital.hmpps.courtregister.jpa.CourtType
import uk.gov.justice.digital.hmpps.courtregister.service.BuildingContactService
import uk.gov.justice.digital.hmpps.courtregister.service.CourtBuildingService
import uk.gov.justice.digital.hmpps.courtregister.service.CourtService
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

// This is a hack to get around the fact that springdocs responses cannot contain generics
class CourtDtoPage : PageImpl<CourtDto>(mutableListOf<CourtDto>())

@RestController
@Validated
@RequestMapping("/courts", produces = [MediaType.APPLICATION_JSON_VALUE])
class CourtResource(
  private val courtService: CourtService,
  private val buildingService: CourtBuildingService,
  private val contactService: BuildingContactService
) {
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

  @GetMapping("/paged")
  @Operation(
    summary = "Get page of active courts",
    description = "Page of courts (active only)",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Page of Active Court Information Returned",
        content = arrayOf(
          Content(
            mediaType = "application/json",
            array = ArraySchema(schema = Schema(implementation = CourtDtoPage::class))
          )
        )
      )
    ]
  )
  fun getPageOfActiveCourts(pageable: Pageable = Pageable.unpaged()): Page<CourtDto> =
    courtService.findPage(true, pageable)

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

  @GetMapping("/all/paged")
  @Operation(
    summary = "Get page of active and inactive courts",
    description = "Page of active/inactive courts",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "All Court Information Returned (Active only)",
        content = arrayOf(
          Content(
            mediaType = "application/json",
            array = ArraySchema(schema = Schema(implementation = CourtDtoPage::class))
          )
        )
      )
    ]
  )
  fun getPageOfCourts(pageable: Pageable = Pageable.unpaged()): Page<CourtDto> =
    courtService.findPage(false, pageable)

  @GetMapping("/id/{courtId}/buildings/id/{buildingId}")
  @Operation(
    summary = "Get specified building",
    description = "Information on a specific building",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Building Information Returned",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = BuildingDto::class))]
      ),
      ApiResponse(
        responseCode = "400",
        description = "Incorrect request to get building information",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]
      ),
      ApiResponse(
        responseCode = "404",
        description = "Building ID not found",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]
      )
    ]
  )
  fun getBuildingFromId(
    @Schema(description = "Court ID", example = "BRMNCC", required = true)
    @PathVariable @Size(max = 12, min = 2, message = "Court ID must be between 2 and 12") courtId: String,
    @Schema(description = "Building ID", example = "234231", required = true)
    @PathVariable buildingId: Long
  ): BuildingDto =
    buildingService.findById(courtId, buildingId)

  @GetMapping("/buildings/sub-code/{subCode}")
  @Operation(
    summary = "Get specified building by sub-code",
    description = "Information on a specific building by sub-code",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Building Information Returned",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = BuildingDto::class))]
      ),
      ApiResponse(
        responseCode = "400",
        description = "Incorrect request to get building information",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]
      ),
      ApiResponse(
        responseCode = "404",
        description = "Building SubCode not found",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]
      )
    ]
  )
  fun getBuildingFromSubCode(
    @Schema(description = "Building Sub Code", example = "BCCACC", required = true)
    @PathVariable @Size(max = 12, min = 2, message = "Building Sub code must be between 2 and 12") subCode: String
  ): BuildingDto =
    buildingService.findBySubCode(subCode)

  @GetMapping("/id/{courtId}/buildings/id/{buildingId}/contacts/id/{contactId}")
  @Operation(
    summary = "Get specified contact",
    description = "Information on a specific contact",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Contact Information Returned",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ContactDto::class))]
      ),
      ApiResponse(
        responseCode = "400",
        description = "Incorrect request to get contact information",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]
      ),
      ApiResponse(
        responseCode = "404",
        description = "Contact ID not found",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]
      )
    ]
  )
  fun getContactFromId(
    @Schema(description = "Court ID", example = "ACCRYC", required = true)
    @PathVariable @Size(max = 12, min = 2, message = "Court ID must be between 2 and 12") courtId: String,
    @Schema(description = "Building ID", example = "234231", required = true)
    @PathVariable buildingId: Long,
    @Schema(description = "Contact ID", example = "11111", required = true) @PathVariable contactId: Long
  ): ContactDto =
    contactService.findById(courtId, buildingId, contactId)
}

@JsonInclude(NON_NULL)
@Schema(description = "Court Information")
data class CourtDto(
  @Schema(description = "Court ID", example = "ACCRYC", required = true) @field:Size(
    max = 12,
    min = 2,
    message = "Court ID must be between 2 and 12"
  ) @NotBlank val courtId: String,
  @Schema(description = "Name of the court", example = "Accrington Youth Court", required = true) @field:Size(
    max = 80,
    min = 2,
    message = "Court name must be between 2 and 80"
  ) @NotBlank val courtName: String,
  @Schema(description = "Description of the court", example = "Accrington Youth Court", required = false) @field:Size(
    max = 200,
    min = 2,
    message = "Court name must be between 2 and 200"
  ) val courtDescription: String?,
  @Schema(description = "Type of court with description", required = true) val type: CourtTypeDto,
  @Schema(description = "Whether the court is still active", required = true) val active: Boolean,
  @Schema(description = "List of buildings for this court entity") val buildings: List<BuildingDto>? = listOf()
) {
  constructor(court: Court) : this(
    court.id, court.courtName, court.courtDescription, CourtTypeDto(court.courtType), court.active,
    court.buildings?.map { BuildingDto(it) }
  )

  @JsonProperty
  @Schema(description = "Type of court", example = "COU")
  fun courtType(): String = type.courtType
}

@JsonInclude(NON_NULL)
@Schema(description = "Court Type")
data class CourtTypeDto(
  @Schema(description = "Type of court", example = "COU", required = true) val courtType: String,
  @Schema(
    description = "Description of the type of court",
    example = "County Court/County Divorce Ct",
    required = true
  ) @NotBlank val courtName: String
) {
  constructor(courtType: CourtType) : this(courtType.id, courtType.description)
}

@JsonInclude(NON_NULL)
@Schema(description = "Building")
data class BuildingDto(
  @Schema(description = "Unique ID of the building", example = "10000", required = true) val id: Long,
  @Schema(description = "Court Id for this building", example = "ACCRYC") val courtId: String,
  @Schema(description = "Sub location code for referencing building", example = "AAABBB") val subCode: String?,
  @Schema(description = "Building Name", example = "Crown House") val buildingName: String?,
  @Schema(description = "Street Number and Name", example = "452 West Street") val street: String?,
  @Schema(description = "Locality", example = "West Cross") val locality: String?,
  @Schema(description = "Town/City", example = "Swansea") val town: String?,
  @Schema(description = "County", example = "South Glamorgan") val county: String?,
  @Schema(description = "Postcode", example = "SA3 4HT") val postcode: String?,
  @Schema(description = "Country", example = "UK") val country: String?,
  @Schema(description = "List of contacts for this building by type") val contacts: List<ContactDto>? = listOf()
) {
  constructor(building: Building) : this(
    building.id!!, building.court.id, building.subCode, building.buildingName, building.street, building.locality,
    building.town, building.county, building.postcode, building.country, building.contacts?.map { ContactDto(it) }
  )
}

@JsonInclude(NON_NULL)
@Schema(description = "Contact")
data class ContactDto(
  @Schema(description = "Unique ID of the contact", example = "10000", required = true) val id: Long,
  @Schema(description = "Court Id for this contact", example = "ACCRYC") val courtId: String,
  @Schema(description = "Building Id for this contact", example = "12312") val buildingId: Long,
  @Schema(
    description = "Type of contact",
    example = "TEL",
    required = true,
    allowableValues = ["TEL", "FAX"]
  ) val type: String,
  @Schema(description = "Details of the contact", example = "555 55555", required = true) val detail: String?,
) {
  constructor(contact: Contact) : this(
    contact.id!!,
    contact.building.court.id,
    contact.building.id!!,
    contact.type,
    contact.detail
  )
}
