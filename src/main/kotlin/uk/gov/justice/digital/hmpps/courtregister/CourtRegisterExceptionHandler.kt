package uk.gov.justice.digital.hmpps.courtregister

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import jakarta.persistence.EntityExistsException
import jakarta.persistence.EntityNotFoundException
import jakarta.validation.ValidationException
import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

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

  @ExceptionHandler(MethodArgumentNotValidException::class)
  fun handleValidationAnyException(e: MethodArgumentNotValidException): ResponseEntity<ErrorResponse?>? {
    log.info("Validation exception: {}", e.message)
    return ResponseEntity
      .status(HttpStatus.BAD_REQUEST)
      .body(ErrorResponse(status = HttpStatus.BAD_REQUEST, developerMessage = e.message, errors = e.asErrorList()))
  }

  companion object {
    val log = LoggerFactory.getLogger(CourtRegisterExceptionHandler::class.java)
  }
}

private fun MethodArgumentNotValidException.asErrorList(): List<String> =
  this.allErrors.mapNotNull { it.defaultMessage }

@JsonInclude(NON_NULL)
data class ErrorResponse(
  val status: Int,
  val errorCode: Int? = null,
  val userMessage: String? = null,
  val developerMessage: String? = null,
  val moreInfo: String? = null,
  val errors: List<String>? = null,
) {
  constructor(
    status: HttpStatus,
    errorCode: Int? = null,
    userMessage: String? = null,
    developerMessage: String? = null,
    moreInfo: String? = null,
    errors: List<String>? = null,
  ) :
    this(status.value(), errorCode, userMessage, developerMessage, moreInfo, errors)
}
