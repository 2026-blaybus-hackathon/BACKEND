package com.blaybus.backend.repository

import com.blaybus.backend.entity.Feedback
import com.blaybus.backend.exception.CustomException
import com.blaybus.backend.exception.ErrorCode
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

fun FeedbackRepository.getByFeedbackId(feedbackId: Long): Feedback =
    findById(feedbackId).orElseThrow { CustomException(ErrorCode.FEEDBACK_NOT_FOUND) }

fun FeedbackRepository.getFeedbackWithTaskAndMentorById(feedbackId: Long): Feedback =
    findFeedbackById(feedbackId) ?: throw CustomException(ErrorCode.FEEDBACK_NOT_FOUND)

@Repository
interface FeedbackRepository : JpaRepository<Feedback, Long> {
    fun countByTaskDailyPlannerUserIdAndIsReadFalse(userId: Long): Long

    @EntityGraph(attributePaths = ["task", "mentor", "task.dailyPlanner", "task.dailyPlanner.user"])
    fun findByTaskDailyPlannerUserIdAndIsReadFalse(userId: Long): List<Feedback>

    @EntityGraph(attributePaths = ["task", "mentor", "task.studyImages", "task.dailyPlanner", "task.dailyPlanner.user"])
    fun findFeedbackById(id: Long): Feedback?
}
