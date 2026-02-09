package com.blaybus.backend.service

import com.blaybus.backend.dto.FeedbackDto
import com.blaybus.backend.dto.StudyImageDto
import com.blaybus.backend.dto.mapper.toEmptyFeedbackResponse
import com.blaybus.backend.dto.mapper.toGetFeedbackOfTaskResponse
import com.blaybus.backend.entity.Feedback
import com.blaybus.backend.exception.CustomException
import com.blaybus.backend.exception.ErrorCode
import com.blaybus.backend.repository.FeedbackRepository
import com.blaybus.backend.repository.ObjectStorageRepository
import com.blaybus.backend.repository.TaskRepository
import com.blaybus.backend.repository.UserRepository
import com.blaybus.backend.repository.getByTaskId
import com.blaybus.backend.repository.getByUserId
import com.blaybus.backend.repository.getFeedbackWithTaskAndMentorById
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
class FeedbackService(
    private val userRepository: UserRepository,
    private val feedbackRepository: FeedbackRepository,
    private val taskRepository: TaskRepository,
    private val dailyPlannerService: DailyPlannerService,
    private val objectStorageRepository: ObjectStorageRepository,
    private val authService: AuthService,
) {
    @Transactional
    fun provideFeedbackForMenteesTask(
        mentorId: Long,
        taskId: Long,
        request: FeedbackDto.CreateFeedbackRequest,
    ): Long {
        // 피드백을 작성할 수 있는 task인지 검증val mentor = userRepository.getByUserId(mentorId)
        val mentor = userRepository.getByUserId(mentorId)
        val task = taskRepository.getByTaskId(taskId)
        mentor.validateMentee(task.dailyPlanner.user)
        val createdFeedback =
            feedbackRepository.save(
                Feedback(
                    task,
                    mentor,
                    request.summary.keepContent,
                    request.summary.problemContent,
                    request.summary.tryContent,
                    request.content,
                ),
            )

        return createdFeedback.id
    }

    @Transactional
    fun provideTotalFeedbackForMenteesDailyPlanner(
        mentorId: Long,
        date: LocalDate,
        request: FeedbackDto.CreateTotalFeedbackRequest,
    ) {
        val mentor = userRepository.getByUserId(mentorId)
        val mentee = userRepository.getByUserId(request.menteeId)
        val dailyPlanner = dailyPlannerService.getOrCreateDailyPlannerByDate(mentee, date)
        mentor.validateMentee(dailyPlanner.user)
        dailyPlanner.updateTotalFeedback(request.content)
    }

    @Transactional(readOnly = true)
    fun findFeedbackOfTask(
        menteeId: Long,
        taskId: Long,
    ): FeedbackDto.GetFeedbackOfTaskResponse {
        val mentee = userRepository.getByUserId(menteeId)
        val task = taskRepository.getByTaskId(taskId) // TODO : 피드백이 Lazy 로딩이라서 같이 가져오지 않음
        // API를 요청한 사람과 task의 주인이 동일한지 검증
        task.dailyPlanner.user.validateSameUser(mentee)
        val feedback = task.feedback

        return feedback?.toGetFeedbackOfTaskResponse()
            ?: task.toEmptyFeedbackResponse()
    }

    @Transactional(readOnly = true)
    fun findFeedbacksByMenteeId(
        menteeId: Long,
        mentorId: Long,
        date: LocalDate,
    ): List<FeedbackDto.GetFeedbackOfTaskResponse> {
        val mentor = userRepository.getByUserId(mentorId)
        val mentee = userRepository.getByUserId(menteeId)
        mentor.validateMentee(mentee)
        val start = date.atStartOfDay()
        val end = date.plusDays(1).atStartOfDay()

        return taskRepository
            .findByUserIdAndTaskCreatedBetween(
                menteeId,
                start,
                end,
            ).map(Feedback::toGetFeedbackOfTaskResponse)
    }

    @Transactional(readOnly = true)
    fun findFeedbacks(
        userId: Long,
        menteeId: Long?,
        date: LocalDate,
    ): List<FeedbackDto.GetFeedbackOfTaskResponse> {
        val user = userRepository.getByUserId(userId)
        val targetUser = authService.validateMentorAccessAndGetTargetUser(user, menteeId)
        val start = date.atStartOfDay()
        val end = date.plusDays(1).atStartOfDay()

        return taskRepository
            .findByUserIdAndTaskCreatedBetween(
                targetUser.id,
                start,
                end,
            ).map(Feedback::toGetFeedbackOfTaskResponse)
    }

    @Transactional(readOnly = true)
    fun findTotalFeedbackOfDailyPlanner(
        userId: Long,
        menteeId: Long?,
        date: LocalDate,
    ): FeedbackDto.GetTotalFeedbackResponse {
        val user = userRepository.getByUserId(userId)
        val targetUser = authService.validateMentorAccessAndGetTargetUser(user, menteeId)

        val dailyPlanner = dailyPlannerService.getDailyPlannerByDate(targetUser, date)

        return FeedbackDto.GetTotalFeedbackResponse(
            dailyPlanner.totalFeedback,
        )
    }

    @Transactional(readOnly = true)
    fun unreadFeedbackCount(userId: Long): FeedbackDto.GetUnreadFeedbackCountResponse {
        userRepository.getByUserId(userId)
        return FeedbackDto.GetUnreadFeedbackCountResponse(
            feedbackRepository.countByTaskDailyPlannerUserIdAndIsReadFalse(
                userId,
            ),
        )
    }

    @Transactional(readOnly = true)
    fun getUnreadFeedbacks(userId: Long): List<FeedbackDto.GetUnreadFeedbackResponse> {
        userRepository.getByUserId(userId)
        return feedbackRepository
            .findByTaskDailyPlannerUserIdAndIsReadFalse(userId)
            .map { FeedbackDto.GetUnreadFeedbackResponse(it) }
    }

    @Transactional
    fun getByFeedbackId(
        userId: Long,
        feedbackId: Long,
    ): FeedbackDto.GetFeedbackByIdResponse {
        val feedback = feedbackRepository.getFeedbackWithTaskAndMentorById(feedbackId)
        if (feedback.task.dailyPlanner.user.id != userId) {
            throw CustomException(ErrorCode.NOT_SAME_USER)
        }
        val task = feedback.task
        val studyImageList =
            task.studyImages.map {
                StudyImageDto.StudyImageResponse(it, objectStorageRepository.getDownloadUrl(it.imageFileName))
            }
        feedback.isRead = true
        return FeedbackDto.GetFeedbackByIdResponse(
            feedback,
            StudyImageDto.StudyCertificationResponse(task, studyImageList),
        )
    }

    @Transactional(readOnly = true)
    fun getMenteesWithFeedbackStatus(mentorId: Long): List<FeedbackDto.MentorMenteeListResponse> {
        val mentor = userRepository.getByUserId(mentorId)
        return mentor.mentees.map { mentee ->
            val isPending = taskRepository.existsCompletedMentorTaskWithoutFeedback(mentee.id, mentorId)
            FeedbackDto.MentorMenteeListResponse(
                id = mentee.id,
                name = mentee.name,
                profileUrl = mentee.profileName?.let { objectStorageRepository.getDownloadUrl(it) },
                schoolName = mentee.schoolName,
                grade = mentee.grade?.description,
                feedbackStatus = if (isPending) "PENDING" else "COMPLETED",
            )
        }
    }
}
