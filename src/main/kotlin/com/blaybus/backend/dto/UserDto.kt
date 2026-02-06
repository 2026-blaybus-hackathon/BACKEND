package com.blaybus.backend.dto

import com.blaybus.backend.entity.Role
import com.blaybus.backend.entity.User

class UserDto {
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
}
