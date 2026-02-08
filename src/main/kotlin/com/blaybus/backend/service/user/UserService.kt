package com.blaybus.backend.service.user

import com.blaybus.backend.dto.DailyAchievementRate
import com.blaybus.backend.dto.MenteeProfileResponse
import com.blaybus.backend.dto.SimpleUserResponse
import com.blaybus.backend.dto.UpdateProfileRequest
import com.blaybus.backend.dto.UserProfileResponse
import com.blaybus.backend.dto.UserTodayStudyTimeResponse
import com.blaybus.backend.dto.mapper.toMenteeProfileResponse
import com.blaybus.backend.dto.mapper.toUserProfileResponse
import com.blaybus.backend.repository.ObjectStorageRepository
import com.blaybus.backend.repository.ObjectStorageRepository.Companion.PROFILE_IMAGE_PATH
import com.blaybus.backend.repository.UserRepository
import com.blaybus.backend.repository.getByUserId
import com.blaybus.backend.service.DailyPlannerService
import com.blaybus.backend.service.TaskService
import com.blaybus.backend.util.getWeekRange
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDate

@Service
class UserService(
    private val userRepository: UserRepository,
    private val taskService: TaskService,
    private val objectStorageRepository: ObjectStorageRepository,
    private val dailyPlannerService: DailyPlannerService,
) {
    fun findAllUser(): List<SimpleUserResponse> =
        userRepository.findAll().map {
            SimpleUserResponse(it)
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
    fun getDailyStudyAmount(
        userId: Long,
        date: LocalDate,
    ): UserTodayStudyTimeResponse {
        val user = userRepository.getByUserId(userId)
        val todayTasks = taskService.getTodayTasksForUser(user, date)
        return UserTodayStudyTimeResponse(todayTasks.mapNotNull { it.studyDurationInMinutes }.sum())
    }

    @Transactional
    fun updateProfile(
        userId: Long,
        request: UpdateProfileRequest,
    ) {
        val user = userRepository.getByUserId(userId)
        user.name = request.name
        user.schoolName = request.schoolName
        user.grade = request.grade
        user.targetSchool = request.targetSchool
        user.targetDate = request.targetDate
    }

    @Transactional
    fun updateProfileImage(
        userId: Long,
        profileImage: MultipartFile?,
    ) {
        val user = userRepository.getByUserId(userId)
        user.profileName?.let { objectStorageRepository.delete(it) }
        if (profileImage != null && !profileImage.isEmpty) {
            val storedFileName = objectStorageRepository.upload(PROFILE_IMAGE_PATH, profileImage)
            user.profileName = storedFileName
        } else {
            user.profileName = null
        }
    }

    @Transactional(readOnly = true)
    fun getWeeklyAchievement(
        userId: Long,
        date: LocalDate,
    ): List<DailyAchievementRate> {
        userRepository.getByUserId(userId)
        val (startOfWeek, endOfWeek) = getWeekRange(date)
        val dailyPlannerList = dailyPlannerService.getDailyPlannerByPeriod(userId, startOfWeek, endOfWeek)
        var day = startOfWeek
        val weeklyAchievementList: MutableList<DailyAchievementRate> = mutableListOf()
        for (dailyPlanner in dailyPlannerList) {
            val tasks = dailyPlanner.tasks
            while (day.isBefore(dailyPlanner.date)) {
                weeklyAchievementList.add(DailyAchievementRate(day, 0, 0))
                day = day.plusDays(1)
            }
            weeklyAchievementList.add(
                DailyAchievementRate(
                    day,
                    completedTasks = tasks.count { it.isCompleted },
                    totalTasks = tasks.size,
                ),
            )
            day = day.plusDays(1)
        }
        return weeklyAchievementList
    }

    @Transactional(readOnly = true)
    fun searchMenteesByName(
        mentorId: Long,
        name: String,
    ): List<MenteeProfileResponse> =
        userRepository.findByMentorIdAndNameContainingIgnoreCase(mentorId, name).map { mentee ->
            MenteeProfileResponse(
                mentee,
                mentee.profileName?.let {
                    objectStorageRepository.getDownloadUrl(it)
                },
            )
        }
}
