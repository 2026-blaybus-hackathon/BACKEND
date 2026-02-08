package com.blaybus.backend.dto

import com.blaybus.backend.entity.Grade
import com.blaybus.backend.entity.Role
import com.blaybus.backend.entity.User
import com.blaybus.backend.util.getDDay
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import java.time.LocalDate

data class UpdateProfileRequest(
    @field:NotBlank(message = "이름은 필수 입력값입니다.")
    @field:Schema(description = "이름", example = "홍길동")
    val name: String,
    @field:Schema(description = "출신 학교 (null일 경우 기존 값 없어짐)")
    val schoolName: String?,
    @field:Schema(
        description = "학년(null일 경우 기존 값 없어짐)",
        example = "FIRST",
        allowableValues = ["FIRST", "SECOND", "THIRD", "DROPOUT", "GRADUATED"],
    )
    val grade: Grade?,
    @NotBlank(message = "목표 학교는 필수 입력값입니다.")
    @field:Schema(description = "목표 학교", example = "서울대학교")
    val targetSchool: String,
    @field:Schema(description = "목표일(null일 경우 기존 값 없어짐)")
    val targetDate: LocalDate,
)

data class SimpleUserResponse(
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

data class UserTodayStudyTimeResponse(
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
    @Schema(description = "목표일로부터 디데이")
    val targetDate: Int?,
) {
    constructor(user: User, profileUrl: String?) : this(
        menteeId = user.id,
        name = user.name,
        profileUrl = profileUrl,
        schoolName = user.schoolName,
        grade = user.grade?.description,
        targetSchool = user.targetSchool,
        targetDate = user.targetDate?.let { getDDay(it) },
    )
}

data class UserProfileResponse(
    @Schema(description = "이름")
    val name: String,
    @Schema(description = "프로필 사진명")
    val profileName: String?,
    @Schema(description = "프로필 URL")
    val profileUrl: String?,
    @Schema(description = "출신 학교")
    val schoolName: String?,
    @Schema(description = "학년")
    val grade: String?,
    @Schema(description = "목표 학교")
    val targetSchool: String?,
    @Schema(description = "목표일로부터 디데이")
    val targetDate: Int?,
) {
    constructor(user: User, profileUrl: String?) : this(
        name = user.name,
        profileName = user.originFileName,
        profileUrl = profileUrl,
        schoolName = user.schoolName,
        grade = user.grade?.description,
        targetSchool = user.targetSchool,
        targetDate = user.targetDate?.let { getDDay(it) },
    )
}
