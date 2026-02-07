package com.blaybus.backend.dto

import com.blaybus.backend.entity.Role
import com.blaybus.backend.entity.User
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

data class SimpleUserDto(
    val id: Long,
    val name: String,
    val email: String,
    val role: Role,
) {
    constructor(user: User) : this(
        id = user.id,
        name = user.name,
        email = user.email,
        role = user.role,
    )
}

data class UserTodayStudyTimeDto(
    val todayStudyTime: Int,
)

data class MenteeProfileResponse(
    @Schema(description = "멘티 ID")
    val menteeId: Long,
    @Schema(description = "멘티 이름")
    val name: String,
    @Schema(description = "멘티 프로필 URL")
    val profileUrl: String?,
    @Schema(description = "학교 이름")
    val schoolName: String?,
    @Schema(description = "학년")
    val grade: String?,
    @Schema(description = "목표 학교")
    val targetSchool: String?,
    @Schema(description = "목표일")
    val targetDate: LocalDate?,
) {
    constructor(user: User, profileUrl: String?) : this(
        menteeId = user.id,
        name = user.name,
        profileUrl = profileUrl,
        schoolName = user.schoolName,
        grade = user.grade?.description,
        targetSchool = user.targetSchool,
        targetDate = user.targetDate,
    )
}


data class UserProfileResponse(
    @Schema(description = "이름")
    val name: String,
    @Schema(description = "프로필 URL")
    val profileUrl: String?,
    @Schema(description = "출신 학교")
    val schoolName: String?,
    @Schema(description = "학년")
    val grade: String?,
    @Schema(description = "목표 학교")
    val targetSchool: String?,
    @Schema(description = "목표일")
    val targetDate: LocalDate?,
) {
    constructor(user: User, profileUrl: String?) : this(
        name = user.name,
        profileUrl = profileUrl,
        schoolName = user.schoolName,
        grade = user.grade?.description,
        targetSchool = user.targetSchool,
        targetDate = user.targetDate,
    )
}
