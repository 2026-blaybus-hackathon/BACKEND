package com.blaybus.backend.dto

import com.blaybus.backend.entity.Grade
import com.blaybus.backend.entity.Role
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import java.time.LocalDate

// Request DTOs
data class EmailSignupRequest(
    @field:Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    @field:Email
    @field:NotBlank(message = "이메일은 필수 입력값입니다")
    val email: String,
    @field:NotBlank(message = "이름은 필수 입력값입니다")
    val name: String,
    @field:Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    @field:NotBlank(message = "비밀번호는 필수 입력값입니다")
    val password: String,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "사용자 역할", allowableValues = ["MENTOR", "MENTEE"])
    val role: Role,
    @Schema(description = "학교 이름")
    val schoolName: String?,
    @Schema(description = "학년")
    val grade: Grade,
    @Schema(description = "목표 학교", required = true)
    val targetSchool: String,
    @Schema(description = "목표 시험일", example = "2024-11-15")
    val targetExamDate: LocalDate,
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
    val name: String?,
    // TODO: 학적, 목표, role 등 추가 정보
)

data class LoginResponse(
    val accessToken: String,
    val name: String?,
    // TODO: 학적, 목표, role 등 추가 정보
)
