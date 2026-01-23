package com.blaybus.backend.security

import com.blaybus.backend.constants.ALLOWED_HEADERS_CSV
import com.blaybus.backend.constants.isAllowedOrigin
import com.blaybus.backend.dto.ErrorResponse
import com.blaybus.backend.exception.CustomException
import com.blaybus.backend.exception.ErrorCode
import com.blaybus.backend.repository.auth.BlackListAccessTokenRepository
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(
    private val jwtTokenProvider: JwtTokenProvider,
    private val objectMapper: ObjectMapper,
    private val blackListAccessTokenRepository: BlackListAccessTokenRepository,
) : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val bearerToken = request.getHeader(AUTHORIZATION)
        if (bearerToken.isNullOrBlank() || !bearerToken.startsWith("Bearer ")) {
            filterChain.doFilter(request, response)
            return
        }

        val token = bearerToken.substring(7)
        try {
            if (blackListAccessTokenRepository.existsByToken(token)) {
                throw CustomException(ErrorCode.INVALID_TOKEN, "해당 토큰은 블랙리스트에 있습니다.")
            }
            val userId = jwtTokenProvider.getClaim(token, "userId", Long::class.java)
            val authentication =
                UsernamePasswordAuthenticationToken(
                    userId,
                    token,
                    jwtTokenProvider.getAuthentication(token),
                )
            SecurityContextHolder.getContext().authentication = authentication

            filterChain.doFilter(request, response)
        } catch (e: CustomException) {
            SecurityContextHolder.clearContext()
            if (response.isCommitted) {
                return
            }
            response.reset()
            response.status = HttpServletResponse.SC_UNAUTHORIZED
            response.contentType = "application/json;charset=UTF-8"
            val requestOrigin = request.getHeader("Origin")
            if (isAllowedOrigin(requestOrigin)) {
                response.setHeader("Access-Control-Allow-Origin", requestOrigin)
                response.setHeader("Access-Control-Allow-Credentials", "true")
                response.setHeader("Access-Control-Allow-Headers", ALLOWED_HEADERS_CSV)
            }
            val errorResponse =
                ErrorResponse(
                    e.errorCode.code,
                    e.message,
                )
            objectMapper.writeValue(response.getOutputStream(), errorResponse)
        }
    }
}
