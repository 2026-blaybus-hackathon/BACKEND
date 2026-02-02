package com.blaybus.backend.dto

import com.blaybus.backend.entity.Role
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

// Request DTOs
data class EmailSignupRequest(
    @field:Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    @field:Email
    @field:NotBlank(message = "이메일은 필수 입력값입니다")
    val email: String,
    @field:NotBlank(message = "이름은 필수 입력값입니다")
    val name: String,
    @field:Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    @field:NotBlank(message = "닉네임은 필수 입력값입니다")
    val nickname: String,
    @field:Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    @field:NotBlank(message = "비밀번호는 필수 입력값입니다")
    val password: String,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "사용자 역할", allowableValues = ["MENTOR", "MENTEE"])
    val role: Role,
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

// Response DTOs
data class TokenResponse(
    val accessToken: String,
    val refreshToken: String?,
    val nickname: String?,
)

data class LoginResponse(
    val accessToken: String,
    val nickname: String?,
)
