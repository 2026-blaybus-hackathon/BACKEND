package com.blaybus.backend.service.user

import com.blaybus.backend.dto.UserInfoResponse
import com.blaybus.backend.dto.UserDto.SimpleUserDto
import com.blaybus.backend.dto.mapper.toMenteesResponse
import com.blaybus.backend.entity.User
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

    // TODO: 아래 두 개의 메서드에 대해서 임시로 기본 정보만 응답 - 디자인 확정 후 수정
    @Transactional(readOnly = true)
    fun findAllMentees(mentorId: Long): List<UserInfoResponse> {

        val mentor = userRepository.getByUserId(mentorId)

        return mentor.mentees
            .map(User::toMenteesResponse)
    }

    @Transactional(readOnly = true)
    fun findMyProfile(userId: Long): UserInfoResponse {

        val user = userRepository.getByUserId(userId)

        return user.toMenteesResponse()
    }
}
