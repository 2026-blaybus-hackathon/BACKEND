package com.blaybus.backend.dto

import com.blaybus.backend.entity.Grade
import com.blaybus.backend.entity.Role
import com.blaybus.backend.entity.User
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
    val grade: Grade?,
    @Schema(description = "목표 학교", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "목표 학교는 필수 입력값입니다")
    val targetSchool: String,
    @Schema(description = "목표일", example = "2024-11-15")
    val targetDate: LocalDate?,
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
    @Schema(description = "액세스 토큰")
    val accessToken: String,
    @Schema(description = "리프레시 토큰")
    val refreshToken: String,
    @Schema(description = "사용자 이름")
    val name: String,
    @Schema(description = "사용자 역할")
    val role: String,
    @Schema(description = "이메일")
    val email: String,
    @Schema(description = "사용자 출신 학교")
    val schoolName: String?,
    @Schema(description = "사용자 학년")
    val grade: String?,
    @Schema(description = "사용자 목표 학교")
    val targetSchool: String?,
    @Schema(description = "사용자 목표일")
    val targetDate: LocalDate?,
) {
    constructor(accessToken: String, refreshToken: String, user: User) : this(
        accessToken = accessToken,
        refreshToken = refreshToken,
        name = user.name,
        role = user.role.name,
        email = user.email,
        schoolName = user.schoolName,
        grade = user.grade?.description,
        targetSchool = user.targetSchool,
        targetDate = user.targetDate,
    )
}

data class LoginResponse(
    @Schema(description = "액세스 토큰")
    val accessToken: String,
    @Schema(description = "사용자 이름")
    val name: String,
    @Schema(description = "사용자 역할")
    val role: String,
    @Schema(description = "이메일")
    val email: String,
    @Schema(description = "사용자 출신 학교")
    val schoolName: String?,
    @Schema(description = "사용자 학년")
    val grade: String?,
    @Schema(description = "사용자 목표 학교")
    val targetSchool: String?,
    @Schema(description = "사용자 목표일")
    val targetDate: LocalDate?,
) {
    constructor(tokenResponse: TokenResponse) : this(
        accessToken = tokenResponse.accessToken,
        name = tokenResponse.name,
        role = tokenResponse.role,
        email = tokenResponse.email,
        schoolName = tokenResponse.schoolName,
        grade = tokenResponse.grade,
        targetSchool = tokenResponse.targetSchool,
        targetDate = tokenResponse.targetDate,
    )
}
