package uk.gov.justice.digital.hmpps.courtregister

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import javax.persistence.EntityExistsException
import javax.persistence.EntityNotFoundException
import javax.validation.ValidationException

@RestControllerAdvice
class CourtRegisterExceptionHandler {
  @ExceptionHandler(EntityNotFoundException::class)
  fun handleNotFoundException(e: Exception): ResponseEntity<ErrorResponse> {
    log.debug("Court not found exception: {}", e.message)
    return ResponseEntity
      .status(HttpStatus.NOT_FOUND)
      .body(ErrorResponse(status = HttpStatus.NOT_FOUND, developerMessage = e.message))
  }

  @ExceptionHandler(EntityExistsException::class)
  fun handleExistsException(e: Exception): ResponseEntity<ErrorResponse> {
    log.debug("Court already exists exception: {}", e.message)
    return ResponseEntity
      .status(HttpStatus.CONFLICT)
      .body(ErrorResponse(status = HttpStatus.CONFLICT, developerMessage = e.message))
  }

  @ExceptionHandler(DataIntegrityViolationException::class)
  fun handleConstraintViolationException(e: Exception): ResponseEntity<ErrorResponse> {
    log.debug("Unable to update court due to : {}", e.message)
    return ResponseEntity
      .status(HttpStatus.CONFLICT)
      .body(ErrorResponse(status = HttpStatus.CONFLICT, developerMessage = e.message))
  }

  @ExceptionHandler(ValidationException::class)
  fun handleValidationException(e: java.lang.Exception): ResponseEntity<ErrorResponse?>? {
    log.info("Validation exception: {}", e.message)
    return ResponseEntity
      .status(HttpStatus.BAD_REQUEST)
      .body(ErrorResponse(status = HttpStatus.BAD_REQUEST, developerMessage = e.message))
  }

  companion object {
    val log = LoggerFactory.getLogger(CourtRegisterExceptionHandler::class.java)
  }
}

@JsonInclude(NON_NULL)
data class ErrorResponse(
  val status: Int,
  val errorCode: Int? = null,
  val userMessage: String? = null,
  val developerMessage: String? = null,
  val moreInfo: String? = null
) {
  constructor(
    status: HttpStatus,
    errorCode: Int? = null,
    userMessage: String? = null,
    developerMessage: String? = null,
    moreInfo: String? = null
  ) :
    this(status.value(), errorCode, userMessage, developerMessage, moreInfo)
}
