package com.blaybus.backend.security.handler

import com.blaybus.backend.constants.ALLOWED_HEADERS_CSV
import com.blaybus.backend.constants.isAllowedOrigin
import com.blaybus.backend.dto.ErrorResponse
import com.blaybus.backend.exception.ErrorCode
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.stereotype.Component

@Component
class CustomAuthenticationEntryPoint(
    private val objectMapper: ObjectMapper,
) : AuthenticationEntryPoint {
    override fun commence(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authException: AuthenticationException,
    ) {
        if (response.isCommitted) {
            return
        }
        response.status = HttpServletResponse.SC_UNAUTHORIZED
        response.contentType = "application/json"
        val requestOrigin = request.getHeader("Origin")
        if (isAllowedOrigin(requestOrigin)) {
            response.setHeader("Access-Control-Allow-Origin", requestOrigin)
            response.setHeader("Access-Control-Allow-Credentials", "true")
            response.setHeader("Access-Control-Allow-Headers", ALLOWED_HEADERS_CSV)
        }
        val errorResponse =
            ErrorResponse(
                ErrorCode.UNAUTHENTICATED_ACCESS.code,
                ErrorCode.UNAUTHENTICATED_ACCESS.errorMessage,
            )
        objectMapper.writeValue(response.outputStream, errorResponse) // ErrorResponse 객체를 JSON으로 변환하여 응답 본문에 작성
    }
}
