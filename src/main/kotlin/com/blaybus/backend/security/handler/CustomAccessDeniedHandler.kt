package com.blaybus.backend.security.handler

import com.blaybus.backend.constants.ALLOWED_HEADERS_CSV
import com.blaybus.backend.constants.isAllowedOrigin
import com.blaybus.backend.dto.ErrorResponse
import com.blaybus.backend.exception.ErrorCode
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.web.access.AccessDeniedHandler
import org.springframework.stereotype.Component

@Component
class CustomAccessDeniedHandler(
    private val objectMapper: ObjectMapper,
) : AccessDeniedHandler {
    override fun handle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        accessDeniedException: AccessDeniedException,
    ) {
        response.status = HttpServletResponse.SC_FORBIDDEN
        response.contentType = "application/json"
        val requestOrigin = request.getHeader("Origin")
        if (isAllowedOrigin(requestOrigin)) {
            response.setHeader("Access-Control-Allow-Origin", requestOrigin)
            response.setHeader("Access-Control-Allow-Credentials", "true")
            response.setHeader("Access-Control-Allow-Headers", ALLOWED_HEADERS_CSV)
        }
        val errorResponse =
            ErrorResponse(
                ErrorCode.FORBIDDEN_ACCESS.code,
                ErrorCode.FORBIDDEN_ACCESS.errorMessage,
            )
        objectMapper.writeValue(response.outputStream, errorResponse)
    }
}
