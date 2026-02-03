package com.blaybus.backend.service

import com.blaybus.backend.dto.FeedbackDto
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
}
