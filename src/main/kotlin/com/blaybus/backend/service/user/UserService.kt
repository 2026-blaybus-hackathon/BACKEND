package com.blaybus.backend.service.user

import com.blaybus.backend.dto.UserDto.SimpleUserDto
import com.blaybus.backend.repository.UserRepository
import com.blaybus.backend.repository.getByUserId
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService(
    private val userRepository: UserRepository,
) {
    fun findAllUser(): List<SimpleUserDto> =
        userRepository.findAll().map {
            SimpleUserDto(
                id = it.id,
                nickname = it.nickname,
                email = it.email,
            )
        }

    @Transactional
    fun deleteUser(userId: Long) {
        val user = userRepository.getByUserId(userId)
        userRepository.delete(user)
    }
}
