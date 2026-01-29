package com.blaybus.backend.dto

class UserDto {
    data class SimpleUserDto(
        val id: Long,
        val nickname: String,
        val email: String,
    )
}
