package com.blaybus.backend.service

import com.blaybus.backend.dto.FeedbackDto
import com.blaybus.backend.entity.Feedback
import com.blaybus.backend.exception.CustomException
import com.blaybus.backend.exception.ErrorCode
import com.blaybus.backend.repository.DailyPlannerRepository
import com.blaybus.backend.repository.FeedbackRepository
import com.blaybus.backend.repository.TaskRepository
import com.blaybus.backend.repository.UserRepository
import com.blaybus.backend.repository.getByDailyPlannerId
import com.blaybus.backend.repository.getByTaskId
import com.blaybus.backend.repository.getByUserId
import org.springframework.transaction.annotation.Transactional
import org.springframework.stereotype.Service

@Service
class FeedbackService(
    private val userRepository: UserRepository,
    private val feedbackRepository: FeedbackRepository,
    private val taskRepository: TaskRepository,
    private val dailyPlannerRepository: DailyPlannerRepository,
) {
    fun provideFeedbackForMenteesTask(
        mentorId: Long,
        taskId: Long,
        request: FeedbackDto.CreateFeedbackRequest
    ): Long {

        // 피드백을 작성할 수 있는 task인지 검증
        val mentor = userRepository.getByUserId(mentorId)
        val task = taskRepository.getByTaskId(taskId)
        mentor.validateMentee(task.dailyPlanner.user)
        val createdFeedback = feedbackRepository.save(
            Feedback(
                task,
                mentor,
                request.summary.keepContent,
                request.summary.problemContent,
                request.summary.tryContent,
                request.content
            )
        )

        return createdFeedback.id
    }

    @Transactional
    fun provideTotalFeedbackForMenteesDailyPlanner(
        mentorId: Long,
        dailyPlannerId: Long,
        request: FeedbackDto.CreateTotalFeedbackRequest
    ) {
        val mentor = userRepository.getByUserId(mentorId)
        val dailyPlanner = dailyPlannerRepository.getByDailyPlannerId(dailyPlannerId)
        mentor.validateMentee(dailyPlanner.user)
        dailyPlanner.updateTotalFeedback(request.content)
    }

    @Transactional(readOnly = true)
    fun findFeedbackOfTask(
        menteeId: Long,
        taskId: Long
    ): FeedbackDto.GetFeedbackOfTaskResponse {

        val mentee = userRepository.getByUserId(menteeId)
        val task = taskRepository.getByTaskId(taskId)
        // API를 요청한 사람과 task의 주인이 동일한지 검증
        task.dailyPlanner.user.validateSameUser(mentee)
        val feedback = task.feedback

        return FeedbackDto.GetFeedbackOfTaskResponse(
            keepContent = feedback?.keepContent,
            problemContent = feedback?.problemContent,
            tryContent = feedback?.tryContent,
            detail = feedback?.detail
        )
    }

    @Transactional(readOnly = true)
    fun findTotalFeedbackOfDailyPlanner(
        menteeId: Long,
        dailyPlannerId: Long
    ): FeedbackDto.GetTotalFeedbackResponse {

        val dailyPlanner = dailyPlannerRepository.getByDailyPlannerId(dailyPlannerId)
        val mentee = userRepository.getByUserId(menteeId)
        // API를 요청한 사람과 dailyPlanner의 주인이 동일한지 검증
        dailyPlanner.user.validateSameUser(mentee)

        return FeedbackDto.GetTotalFeedbackResponse(
            dailyPlanner.totalFeedback
        )
    }
}
