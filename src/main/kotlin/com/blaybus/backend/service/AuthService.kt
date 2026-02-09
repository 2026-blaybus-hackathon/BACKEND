package com.blaybus.backend.service

import com.blaybus.backend.entity.Role
import com.blaybus.backend.entity.User
import com.blaybus.backend.exception.CustomException
import com.blaybus.backend.exception.ErrorCode
import com.blaybus.backend.repository.UserRepository
import com.blaybus.backend.repository.getByUserId
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class AuthService(
    private val userRepository: UserRepository,
) {
    fun validateMentorAccessAndGetTargetUser(
        user: User,
        menteeId: Long?,
    ): User {
        val targetUser = menteeId?.let { userRepository.getByUserId(it) } ?: user

        if (user.role == Role.MENTOR) {
            if (menteeId == null) {
                throw CustomException(ErrorCode.MENTEE_ID_REQUIRED)
            }
            user.validateMentee(targetUser)
        }
        return targetUser
    }
}
