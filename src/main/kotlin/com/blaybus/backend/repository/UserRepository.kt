package com.blaybus.backend.repository

import com.blaybus.backend.entity.Provider
import com.blaybus.backend.entity.User
import com.blaybus.backend.exception.CustomException
import com.blaybus.backend.exception.ErrorCode
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

fun UserRepository.getByEmail(email: String): User = findByEmail(email) ?: throw CustomException(ErrorCode.USER_NOT_FOUND)

fun UserRepository.getByUserId(userId: Long): User = findById(userId).orElseThrow { CustomException(ErrorCode.USER_NOT_FOUND) }

@Repository
interface UserRepository : JpaRepository<User, Long> {
    fun findByEmail(email: String): User?

    fun findByEmailAndProvider(
        email: String,
        provider: Provider,
    ): User?

    fun existsByEmail(email: String): Boolean
}
