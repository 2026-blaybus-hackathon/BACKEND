package com.blaybus.backend.controller

import com.blaybus.backend.annotation.SwaggerBody
import com.blaybus.backend.config.JwtProperties
import com.blaybus.backend.dto.EmailLoginRequest
import com.blaybus.backend.dto.EmailSignupRequest
import com.blaybus.backend.dto.LoginResponse
import com.blaybus.backend.dto.TokenResponse
import com.blaybus.backend.exception.CustomException
import com.blaybus.backend.exception.ErrorCode
import com.blaybus.backend.service.auth.AuthService
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Encoding
import jakarta.validation.Valid
import mu.KotlinLogging
import org.springframework.http.HttpCookie
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE
import org.springframework.http.ResponseCookie
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CookieValue
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RequestMapping("/api/v1/auth")
@RestController
class AuthController(
    private val authService: AuthService,
    private val jwtProperties: JwtProperties,
) {
    private val logger = KotlinLogging.logger {}

    @SwaggerBody(
        content = [
            Content(
                encoding = [
                    Encoding(
                        name = "request",
                        contentType = MediaType.APPLICATION_JSON_VALUE,
                    ),
                ],
            ),
        ],
    )
    @PostMapping("/signup/email", consumes = [MULTIPART_FORM_DATA_VALUE])
    fun signupWithEmail(
        @Valid @RequestPart request: EmailSignupRequest,
        @RequestPart(required = false)
        profile: MultipartFile?,
    ): ResponseEntity<Unit> {
        authService.signupWithEmail(request, profile)
        return ResponseEntity.status(HttpStatus.CREATED).build()
    }

    @PostMapping("/login/email")
    fun loginWithEmail(
        @Valid @RequestBody request: EmailLoginRequest,
    ): ResponseEntity<LoginResponse> {
        val tokenResponse: TokenResponse = authService.loginWithEmail(request)
        return ResponseEntity
            .status(HttpStatus.OK)
            .header(HttpHeaders.SET_COOKIE, getRefreshTokenCookie(tokenResponse.refreshToken!!).toString())
            .body(LoginResponse(tokenResponse.accessToken, tokenResponse.nickname))
    }

    @PostMapping("/refresh")
    fun refreshToken(
        @CookieValue("refreshToken") refreshToken: String,
    ): ResponseEntity<LoginResponse> {
        val tokenResponse: TokenResponse = authService.refresh(refreshToken)
        return ResponseEntity
            .status(HttpStatus.OK)
            .header(HttpHeaders.SET_COOKIE, getRefreshTokenCookie(tokenResponse.refreshToken!!).toString())
            .body(LoginResponse(tokenResponse.accessToken, tokenResponse.nickname))
    }

    @PostMapping("/logout")
    fun logout(
        @RequestHeader("Authorization") authHeader: String?,
    ): ResponseEntity<Void> {
        logger.trace("$authHeader logout called")
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw CustomException(ErrorCode.INVALID_TOKEN)
        }
        val accessToken = authHeader.substring(7)
        authService.logout(accessToken)
        return ResponseEntity
            .status(HttpStatus.NO_CONTENT)
            .header(HttpHeaders.SET_COOKIE, getExpiredCookie().toString())
            .build()
    }

    private fun getRefreshTokenCookie(refreshToken: String): HttpCookie =
        ResponseCookie
            .from("refreshToken", refreshToken)
            .httpOnly(true) // JavaScript 에서 쿠키에 접근할 수 없도록
            .maxAge(jwtProperties.refreshTokenExpirationTime * 60) // 쿠키의 만료 시간 설정
            .secure(true) // cookie 가 https 에서만 전송되도록
            .path("/api/v1/auth") // 쿠키가 유효한 경로 설정
            .sameSite("None")
            .build()

    private fun getExpiredCookie(): HttpCookie =
        ResponseCookie
            .from("refreshToken", "")
            .httpOnly(true)
            .maxAge(0)
            .path("/api/v1/auth")
            .build()
}
