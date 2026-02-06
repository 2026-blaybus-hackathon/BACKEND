package com.blaybus.backend.service.user

import com.blaybus.backend.dto.MenteeProfileResponse
import com.blaybus.backend.dto.SimpleUserDto
import com.blaybus.backend.dto.UserProfileResponse
import com.blaybus.backend.dto.UserTodayStudyTimeDto
import com.blaybus.backend.dto.mapper.toMenteeProfileResponse
import com.blaybus.backend.dto.mapper.toUserProfileResponse
import com.blaybus.backend.repository.ObjectStorageRepository
import com.blaybus.backend.repository.UserRepository
import com.blaybus.backend.repository.getByUserId
import com.blaybus.backend.service.TaskService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService(
    private val userRepository: UserRepository,
    private val taskService: TaskService,
    private val objectStorageRepository: ObjectStorageRepository,
) {
    fun findAllUser(): List<SimpleUserDto> =
        userRepository.findAll().map {
            SimpleUserDto(it)
        }

    @Transactional
    fun deleteUser(userId: Long) {
        val user = userRepository.getByUserId(userId)
        userRepository.delete(user)
    }

    // TODO: 아래 두 개의 메서드에 대해서 임시로 기본 정보만 응답 - 디자인 확정 후 수정
    @Transactional(readOnly = true)
    fun findAllMentees(mentorId: Long): List<MenteeProfileResponse> =
        userRepository
            .getByUserId(mentorId)
            .mentees
            .map { mentee ->
                val url =
                    mentee.profileName?.let {
                        objectStorageRepository.getDownloadUrl(it)
                    }
                mentee.toMenteeProfileResponse(url)
            }

    @Transactional(readOnly = true)
    fun findMyProfile(userId: Long): UserProfileResponse {
        val user = userRepository.getByUserId(userId)

        return user.toUserProfileResponse(
            user.profileName?.let(objectStorageRepository::getDownloadUrl),
        )
    }

    @Transactional(readOnly = true)
    fun getDailyStudyAmount(userId: Long): UserTodayStudyTimeDto {
        val user = userRepository.getByUserId(userId)
        val todayTasks = taskService.getTodayTasksForUser(user)
        return UserTodayStudyTimeDto(todayTasks.mapNotNull { it.studyDurationInMinutes }.sum())
    }
}
