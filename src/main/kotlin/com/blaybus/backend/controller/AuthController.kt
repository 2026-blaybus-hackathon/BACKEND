package com.blaybus.backend.controller

import com.blaybus.backend.annotation.ApiErrorCodes
import com.blaybus.backend.config.JwtProperties
import com.blaybus.backend.dto.EmailLoginRequest
import com.blaybus.backend.dto.EmailSendRequest
import com.blaybus.backend.dto.EmailSignupRequest
import com.blaybus.backend.dto.EmailVerifyRequest
import com.blaybus.backend.dto.EmailVerifyResponse
import com.blaybus.backend.dto.LoginResponse
import com.blaybus.backend.dto.TokenResponse
import com.blaybus.backend.exception.ErrorCode
import com.blaybus.backend.service.auth.AuthService
import com.blaybus.backend.service.auth.EmailService
import jakarta.validation.Valid
import org.springframework.http.HttpCookie
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseCookie
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val authService: AuthService,
    private val emailService: EmailService,
    private val jwtProperties: JwtProperties,
) {
    @ApiErrorCodes(
        ErrorCode.REGISTERED_ALREADY,
        ErrorCode.REGISTERED_ANOTHER_SOCIAL,
        ErrorCode.INVALID_NICKNAME_FORMAT,
        ErrorCode.INVALID_PASSWORD_FORMAT,
        ErrorCode.REQUIRED_PASSWORD,
        ErrorCode.REQUIRED_EMAIL_VERIFICATION_TOKEN,
        ErrorCode.REQUIRED_NICKNAME,
        ErrorCode.REQUIRED_NAME,
        ErrorCode.INVALID_EMAIL_FORMAT,
    )
    @PostMapping("/signup/email")
    fun signupWithEmail(
        @Valid @RequestBody request: EmailSignupRequest,
    ): ResponseEntity<Unit> {
        authService.signupWithEmail(request)
        return ResponseEntity.status(HttpStatus.CREATED).build()
    }

    /*@PostMapping("/signup/google")
    fun signUpWithGoogle(
        @RequestBody @Valid request: GoogleSignUpRequest?,
    ): ResponseEntity<LoginResponse> {
        val tokenResponse: TokenResponse = authService.signupWithGoogle(request)
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .header(HttpHeaders.SET_COOKIE, getRefreshTokenCookie(tokenResponse.refreshToken).toString())
            .body(LoginResponse(tokenResponse.accessToken, tokenResponse.nickname))
    }*/

    @PostMapping("/login/email")
    fun loginWithEmail(
        @Valid @RequestBody request: EmailLoginRequest,
    ): ResponseEntity<LoginResponse> {
        val tokenResponse: TokenResponse = authService.loginWithEmail(request)
        return ResponseEntity
            .status(HttpStatus.OK)
            .header(HttpHeaders.SET_COOKIE, getRefreshTokenCookie(tokenResponse.refreshToken).toString())
            .body(LoginResponse(tokenResponse.accessToken, tokenResponse.nickname))
    }

    /*@PostMapping("/login/google")
    fun loginWithGoogle(
        @RequestBody @Valid request: GoogleLoginRequest,
    ): ResponseEntity<LoginResponse> {
        val tokenResponse: TokenResponse = authService.loginWithOauth2(request)
        if (tokenResponse.refreshToken() == null) {
            return ResponseEntity
                .status(HttpStatus.SEE_OTHER)
                .body(LoginResponse(tokenResponse.accessToken(), null))
        }
        return ResponseEntity
            .status(HttpStatus.OK)
            .header(HttpHeaders.SET_COOKIE, getRefreshTokenCookie(tokenResponse.refreshToken()).toString)
            .body<LoginResponse>(LoginResponse(tokenResponse.accessToken, tokenResponse.nickname))
    }

    @PostMapping("/refresh")
    fun refreshToken(
        @CookieValue("refreshToken") refreshToken: String?,
    ): ResponseEntity<LoginResponse?> {
        val tokenResponse: TokenResponse = authService.refresh(refreshToken)
        return ResponseEntity
            .status(HttpStatus.OK)
            .header(HttpHeaders.SET_COOKIE, getRefreshTokenCookie(tokenResponse.refreshToken).toString)
            .body<LoginResponse?>(LoginResponse(tokenResponse.accessToken, tokenResponse.nickname))
    }

    @PostMapping("/validate/nickname")
    fun nicknameDuplicateCheck(
        @RequestBody request: @Valid NicknameDuplicateCheckRequest,
    ): ResponseEntity<NicknameDuplicateCheckResponse?> =
        ResponseEntity
            .status(HttpStatus.OK)
            .body<NicknameDuplicateCheckResponse?>(
                NicknameDuplicateCheckResponse(
                    authService.nicknameDuplicateCheck(
                        request.getNickname,
                    ),
                ),
            )

    @PostMapping("/logout")
    fun logout(
        @RequestHeader("Authorization") authHeader: String,
    ): ResponseEntity<Void?> {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw AuthException(ErrorCode.INVALID_TOKEN)
        }
        val accessToken = authHeader.substring(7)
        authService.logout(accessToken)
        return ResponseEntity
            .status(HttpStatus.NO_CONTENT)
            .header(COOKIE_HEARER, getExpiredCookie.toString)
            .build<Void?>
    }*/
    @PostMapping("email/send")
    fun sendEmail(
        @RequestBody emailRequest: EmailSendRequest,
    ): ResponseEntity<Unit> {
        emailService.sendVerificationEmail(emailRequest.email)
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build()
    }

    @PostMapping("email/verify")
    fun verifyEmail(
        @RequestBody emailVerifyRequest: EmailVerifyRequest,
    ): ResponseEntity<EmailVerifyResponse> =
        ResponseEntity.ok(
            emailService.verifyEmailCode(
                emailVerifyRequest.email,
                emailVerifyRequest.code,
            ),
        )

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
