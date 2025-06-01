package paladin.core.exceptions

import io.github.oshai.kotlinlogging.KLogger
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import paladin.core.configuration.properties.ApplicationConfigurationProperties
import paladin.core.models.response.ErrorResponse

@ControllerAdvice
class ExceptionHandler(private val logger: KLogger, private val config: ApplicationConfigurationProperties) {

    @ExceptionHandler(AccessDeniedException::class)
    fun handleAccessDeniedException(ex: AccessDeniedException): ResponseEntity<ErrorResponse> {
        logger.warn { "Access denied: ${ex.message}" }
        return ErrorResponse(
            statusCode = HttpStatus.FORBIDDEN,
            error = "ACCESS DENIED",
            message = ex.message ?: "Access denied",
            stackTrace = config.includeStackTrace.takeIf { it }?.let { ex.stackTraceToString() }
        ).also { logger.error { it } }.let {
            ResponseEntity(it, it.statusCode)
        }
    }

    @ExceptionHandler(NotFoundException::class)
    fun handleNotFoundException(ex: NotFoundException): ResponseEntity<ErrorResponse> {
        return ErrorResponse(
            statusCode = HttpStatus.NOT_FOUND,
            error = "RESOURCE NOT FOUND",
            message = ex.message ?: "Resource not found",
            stackTrace = config.includeStackTrace.takeIf { it }?.let { ex.stackTraceToString() }
        ).also { logger.error { it } }.let {
            ResponseEntity(it, it.statusCode)
        }
    }
}