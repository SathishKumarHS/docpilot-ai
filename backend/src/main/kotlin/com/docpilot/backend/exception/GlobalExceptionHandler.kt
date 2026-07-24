package com.docpilot.backend.exception

import com.docpilot.backend.aiworker.exception.AiWorkerException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MissingRequestHeaderException
import org.springframework.web.multipart.MultipartException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.multipart.support.MissingServletRequestPartException
import org.springframework.dao.DataIntegrityViolationException

@RestControllerAdvice
class GlobalExceptionHandler {
    private val log = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler(UnsupportedDocumentTypeException::class)
    fun handleUnsupportedDocumentType(
        ex: UnsupportedDocumentTypeException
    ): ResponseEntity<ErrorResponse> {
        log.warn("Unsupported document type: {}", ex.message)
        return ResponseEntity
            .status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
            .body(ErrorResponse(
                status = HttpStatus.UNSUPPORTED_MEDIA_TYPE.value(),
                error = "Unsupported Media Type",
                message = ex.message ?: "Only PDF files are supported"
            ))
    }

    @ExceptionHandler(UnsupportedDocumentSizeException::class)
    fun handleUnsupportedDocumentSize(
        ex: UnsupportedDocumentSizeException
    ): ResponseEntity<ErrorResponse> {
        log.warn("Unsupported document size: {}", ex.message)
        return ResponseEntity
            .status(HttpStatus.PAYLOAD_TOO_LARGE)
            .body(ErrorResponse(
                status = HttpStatus.PAYLOAD_TOO_LARGE.value(),
                error = "Payload Too Large",
                message = ex.message ?: "File size exceeds the maximum allowed limit"
            ))
    }

    @ExceptionHandler(AiWorkerException::class)
    fun handleAiWorkerError(
        ex: AiWorkerException
    ): ResponseEntity<ErrorResponse> {
        log.error("AI worker error: {}", ex.message)
        return ResponseEntity
            .status(HttpStatus.BAD_GATEWAY)
            .body(ErrorResponse(
                status = HttpStatus.BAD_GATEWAY.value(),
                error = "Bad Gateway",
                message = ex.message ?: "AI service is unavailable"
            ))
    }

    @ExceptionHandler(UnauthorizedException::class)
    fun handleUnauthorized(
        ex: UnauthorizedException
    ): ResponseEntity<ErrorResponse> {
        log.warn("Unauthorized: {}", ex.message)
        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(ErrorResponse(
                status = HttpStatus.UNAUTHORIZED.value(),
                error = "Unauthorized",
                message = "Invalid email or password"
            ))
    }

    @ExceptionHandler(NotFoundException::class)
    fun handleNotFound(
        ex: NotFoundException
    ): ResponseEntity<ErrorResponse> {
        log.warn("Not found: {}", ex.message)
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(ErrorResponse(
                status = HttpStatus.NOT_FOUND.value(),
                error = "Not Found",
                message = ex.message ?: "Resource not found"
            ))
    }

    @ExceptionHandler(ConflictException::class)
    fun handleConflict(
        ex: ConflictException
    ): ResponseEntity<ErrorResponse> {
        log.warn("Conflict: {}", ex.message)
        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(ErrorResponse(
                status = HttpStatus.CONFLICT.value(),
                error = "Conflict",
                message = ex.message ?: "Resource already exists"
            ))
    }

    @ExceptionHandler(
        IllegalArgumentException::class,
        MissingRequestHeaderException::class,
        MissingServletRequestParameterException::class,
        MissingServletRequestPartException::class,
        HttpMessageNotReadableException::class,
        MultipartException::class,
    )
    fun handleBadRequest(
        ex: Exception
    ): ResponseEntity<ErrorResponse> {
        log.warn("Bad request: {}: {}", ex.javaClass.simpleName, ex.message)
        val message = when (ex) {
            is MissingRequestHeaderException ->
                "Missing required header: ${ex.headerName}"
            is MultipartException ->
                "File upload error: ${ex.message}"
            else ->
                ex.message ?: "Bad request"
        }
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse(
                status = HttpStatus.BAD_REQUEST.value(),
                error = "Bad Request",
                message = message
            ))
    }

    @ExceptionHandler(DataIntegrityViolationException::class)
    fun handleDataIntegrityViolation(
        ex: DataIntegrityViolationException
    ): ResponseEntity<ErrorResponse> {
        log.error("Data integrity violation: {}", ex.message)
        return ResponseEntity
            .status(HttpStatus.UNPROCESSABLE_ENTITY)
            .body(ErrorResponse(
                status = HttpStatus.UNPROCESSABLE_ENTITY.value(),
                error = "Unprocessable Entity",
                message = "The submitted data contains invalid content"
            ))
    }

    @ExceptionHandler(Exception::class)
    fun handleGeneric(
        ex: Exception
    ): ResponseEntity<ErrorResponse> {
        log.error("Unhandled {}: {}", ex.javaClass.simpleName, ex.message, ex)
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ErrorResponse(
                status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
                error = "Internal Server Error",
                message = ex.message ?: "An unexpected error occurred"
            ))
    }
}
