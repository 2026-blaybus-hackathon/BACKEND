package com.blaybus.backend.service

import com.blaybus.backend.dto.FeedbackDto
import com.blaybus.backend.dto.mapper.toEmptyFeedbackResponse
import com.blaybus.backend.dto.mapper.toGetFeedbackOfTaskResponse
import com.blaybus.backend.entity.Feedback
import com.blaybus.backend.repository.DailyPlannerRepository
import com.blaybus.backend.repository.FeedbackRepository
import com.blaybus.backend.repository.TaskRepository
import com.blaybus.backend.repository.UserRepository
import com.blaybus.backend.repository.getByDailyPlannerId
import com.blaybus.backend.repository.getByTaskId
import com.blaybus.backend.repository.getByUserId
import org.springframework.transaction.annotation.Transactional
import org.springframework.stereotype.Service
import java.time.LocalDate

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
        // 피드백을 작성할 수 있는 task인지 검증val mentor = userRepository.getByUserId(mentorId)
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

        return task.feedback?.toGetFeedbackOfTaskResponse()
            ?: task.toEmptyFeedbackResponse()
    }

    @Transactional(readOnly = true)
    fun findFeedbacksByMenteeId(
        menteeId: Long,
        mentorId: Long,
        date: LocalDate
    ): List<FeedbackDto.GetFeedbackOfTaskResponse> {
        val mentor = userRepository.getByUserId(mentorId)
        val mentee = userRepository.getByUserId(menteeId)
        mentor.validateMentee(mentee)
        val start = date.atStartOfDay()
        val end = date.plusDays(1).atStartOfDay()

        return taskRepository.findByUserIdAndTaskCreatedBetween(
            menteeId,
            start,
            end
        )
            .map(Feedback::toGetFeedbackOfTaskResponse)
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
