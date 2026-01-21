package com.blaybus.backend.exception

import com.blaybus.backend.dto.ErrorResponse
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.servlet.NoHandlerFoundException

@RestControllerAdvice
class GlobalExceptionHandler {
    private val log = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    @ExceptionHandler(CustomException::class)
    fun handleBusinessException(e: CustomException): ResponseEntity<ErrorResponse> {
        val errorCode = e.errorCode
        val errorResponse = ErrorResponse(errorCode.code, errorCode.errorMessage)
        return ResponseEntity.status(errorCode.httpStatus).body(errorResponse)
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleHttpMessageNotReadableException(e: HttpMessageNotReadableException): ResponseEntity<ErrorResponse> {
        log.error("HttpMessageNotReadableException : {}", e.message)
        val errorCode = ErrorCode.BAD_REQUEST_JSON
        val errorResponse = ErrorResponse(errorCode.code, errorCode.errorMessage)
        // Sentry.captureException(e)
        return ResponseEntity.status(errorCode.httpStatus).body(errorResponse)
    }

    @ExceptionHandler(value = [NoHandlerFoundException::class, HttpRequestMethodNotSupportedException::class])
    fun handleNoPageFoundException(e: Exception): ResponseEntity<ErrorResponse> {
        log.error("NoHandlerFoundException : {}", e.message)
        val errorCode = ErrorCode.NOT_FOUND_END_POINT
        val errorResponse = ErrorResponse(errorCode.code, errorCode.errorMessage)
        return ResponseEntity.status(errorCode.httpStatus).body(errorResponse)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleArgumentNotValidException(e: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val errorMessage =
            e.bindingResult.allErrors
                .firstOrNull()
                ?.defaultMessage ?: e.message
        log.error("MethodArgumentNotValidException : {}", errorMessage)
        val errorCode = ErrorCode.INVALID_INPUT_VALUE
        val errorResponse = ErrorResponse(errorCode.code, errorMessage)

        return ResponseEntity.status(errorCode.httpStatus).body(errorResponse)
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleArgumentTypeMismatchException(e: MethodArgumentTypeMismatchException): ResponseEntity<ErrorResponse> {
        log.error("MethodArgumentTypeMismatchException : {}", e.message)
        val errorCode = ErrorCode.INVALID_TYPE_VALUE
        val errorResponse = ErrorResponse(errorCode.code, errorCode.errorMessage)

        return ResponseEntity.status(errorCode.httpStatus).body(errorResponse)
    }

    @ExceptionHandler(MissingServletRequestParameterException::class)
    fun handleMissingServletRequestParameterException(e: MissingServletRequestParameterException): ResponseEntity<ErrorResponse> {
        log.error("MissingServletRequestParameterException : {}", e.message)
        val errorCode = ErrorCode.MISSING_REQUEST_PARAMETER
        val errorResponse = ErrorResponse(errorCode.code, e.message)

        return ResponseEntity.status(errorCode.httpStatus).body(errorResponse)
    }

    @ExceptionHandler(Exception::class)
    fun handleException(e: Exception): ResponseEntity<ErrorResponse> {
        log.error("Exception : ", e)
        val errorCode = ErrorCode.INTERNAL_SERVER_ERROR
        val errorResponse = ErrorResponse(errorCode.code, errorCode.errorMessage)
        return ResponseEntity.status(errorCode.httpStatus).body(errorResponse)
    }
}
