package com.docpilot.backend.exception

import com.docpilot.backend.aiworker.exception.AiWorkerException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MissingRequestHeaderException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.multipart.support.MissingServletRequestPartException

@RestControllerAdvice
class GlobalExceptionHandler {
    private val log = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler(UnsupportedDocumentTypeException::class)
    fun handleUnsupportedDocumentType(
        ex: UnsupportedDocumentTypeException
    ): ResponseEntity<ErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
            .body(ErrorResponse(
                status = HttpStatus.UNSUPPORTED_MEDIA_TYPE.value(),
                message = ex.message ?: "Unsupported document type"
            ))
    }

    @ExceptionHandler(UnsupportedDocumentSizeException::class)
    fun handleUnsupportedDocumentSize(
        ex: UnsupportedDocumentSizeException
    ): ResponseEntity<ErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.PAYLOAD_TOO_LARGE)
            .body(ErrorResponse(
                status = HttpStatus.PAYLOAD_TOO_LARGE.value(),
                message = ex.message ?: "Unsupported document size"
            ))
    }

    @ExceptionHandler(AiWorkerException::class)
    fun handleAiWorkerError(
        ex: AiWorkerException
    ): ResponseEntity<ErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.BAD_GATEWAY)
            .body(ErrorResponse(
                status = HttpStatus.BAD_GATEWAY.value(),
                message = "AI service error: ${ex.message}"
            ))
    }

    @ExceptionHandler(
        IllegalArgumentException::class,
        MissingRequestHeaderException::class,
        MissingServletRequestParameterException::class,
        MissingServletRequestPartException::class,
        HttpMessageNotReadableException::class,
    )
    fun handleBadRequest(
        ex: Exception
    ): ResponseEntity<ErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse(
                status = HttpStatus.BAD_REQUEST.value(),
                message = ex.message ?: "Bad request"
            ))
    }

    @ExceptionHandler(Exception::class)
    fun handleGeneric(
        ex: Exception
    ): ResponseEntity<ErrorResponse> {
        log.error("Unhandled exception", ex)
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ErrorResponse(
                status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
                message = "An unexpected error occurred"
            ))
    }
}
