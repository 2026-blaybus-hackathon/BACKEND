package com.blaybus.backend.dto

import com.blaybus.backend.entity.Role
import com.blaybus.backend.entity.User

data class SimpleUserDto(
    val id: Long,
    val nickname: String,
    val email: String,
    val role: Role,
) {
    constructor(user: User) : this(
        id = user.id,
        nickname = user.nickname,
        email = user.email,
        role = user.role,
    )
}

data class UserTodayStudyTimeDto(
    val todayStudyTime: Int,
)
