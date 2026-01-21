package com.blaybus.backend.dto

import com.blaybus.backend.constants.NICKNAME_MESSAGE
import com.blaybus.backend.constants.NICKNAME_REGEX
import com.blaybus.backend.constants.PASSWORD_MESSAGE
import com.blaybus.backend.constants.PASSWORD_REGEX
import com.blaybus.backend.entity.Provider
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern

// Request DTOs
data class EmailSignupRequest(
    @field:NotBlank(message = "이름은 필수 입력값입니다")
    val name: String,
    @field:Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = NICKNAME_MESSAGE, example = "blaybus123")
    @field:NotBlank(message = "닉네임은 필수 입력값입니다")
    @field:Pattern(regexp = NICKNAME_REGEX, message = NICKNAME_MESSAGE)
    val nickname: String,
    @field:Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = PASSWORD_MESSAGE)
    @field:NotNull(message = "비밀번호는 필수 입력값입니다")
    @field:Pattern(regexp = PASSWORD_REGEX, message = PASSWORD_MESSAGE)
    var password: String,
    @field:Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "이메일 인증 후 사용자에게 발급되는 토큰입니다")
    @field:NotNull(message = "이메일 인증 토큰은 필수 입력값입니다")
    var emailVerifyToken: String,
)

data class GoogleSignUpRequest(
    @field:Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = NICKNAME_MESSAGE)
    @field:NotBlank(message = "닉네임은 필수 입력값입니다")
    @Pattern(
        regexp = NICKNAME_REGEX,
        message = NICKNAME_MESSAGE,
    )
    var nickname: String,
    @field:Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "소셜 로그인시 회원가입 되어있지 않으면 발급되는 소셜 인증 토큰입니다")
    @field:NotBlank(message = "소셜 인증 토큰은 필수 입력값입니다")
    var socialSignUpToken: String,
)

data class EmailLoginRequest(
    @field:Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    @field:Email
    @field:NotBlank(message = "이메일은 필수 입력값입니다")
    val email: String,
    @field:Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    @field:NotBlank(message = "비밀번호는 필수 입력값입니다")
    val password: String,
)

data class GoogleLoginRequest(
    @field:Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "리다이렉트 후 전달되는 OAuth 인증 코드입니다")
    @field:NotBlank(message = "소셜 인증 코드는 필수 입력값입니다")
    var code: String,
    @field:Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "소셜 플랫폼 타입")
    @field:NotBlank(message = "소셜 플랫폼 타입은 필수 입력값입니다")
    private var provider: Provider,
)

data class NicknameDuplicateCheckRequest(
    @field:Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = NICKNAME_MESSAGE)
    @field:NotBlank(message = "닉네임은 필수 입력값입니다")
    @Pattern(
        regexp = NICKNAME_REGEX,
        message = NICKNAME_MESSAGE,
    )
    var nickname: String,
)

data class ResetPasswordRequest(
    @field:Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    @field:NotBlank(message = "비밀번호는 필수 입력값입니다")
    @field:Pattern(regexp = PASSWORD_REGEX, message = PASSWORD_MESSAGE)
    val password: String,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "이메일 인증 후 사용자에게 발급되는 토큰입니다")
    @NotBlank(message = "이메일 인증 토큰은 필수 입력값입니다")
    private val emailVerifyToken: String,
)

data class EmailSendRequest(
    val email: String,
)

data class EmailVerifyRequest(
    val email: String,
    val code: String,
)

// Response DTOs

data class TokenResponse(
    val accessToken: String,
    val refreshToken: String,
    val nickname: String,
)

data class LoginResponse(
    val accessToken: String,
    val nickname: String,
)

data class EmailVerifyResponse(
    val emailVerifyToken: String,
)
