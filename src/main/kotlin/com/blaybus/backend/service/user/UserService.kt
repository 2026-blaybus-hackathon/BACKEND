package com.blaybus.backend.service.user

import com.blaybus.backend.entity.User
import com.blaybus.backend.repository.UserRepository
import com.blaybus.backend.repository.getByUserId
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService(
    private val userRepository: UserRepository,
) {
    public fun findAllUser(): MutableList<User> = userRepository.findAll()

    @Transactional
    public fun deleteUser(userId: Long) {
        val user = userRepository.getByUserId(userId)
        userRepository.delete(user)
    }
}
