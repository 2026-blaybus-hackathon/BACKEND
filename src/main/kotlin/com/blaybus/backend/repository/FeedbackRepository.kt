package com.blaybus.backend.repository

import com.blaybus.backend.entity.Feedback
import com.blaybus.backend.exception.CustomException
import com.blaybus.backend.exception.ErrorCode
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Transactional(readOnly = true)
fun FeedbackRepository.getByFeedbackId(feedbackId: Long): Feedback = findById(feedbackId).orElseThrow { CustomException(ErrorCode.FEEDBACK_NOT_FOUND) }

@Repository
interface FeedbackRepository : JpaRepository<Feedback, Long> {
}
