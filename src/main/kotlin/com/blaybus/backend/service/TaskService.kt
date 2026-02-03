package com.blaybus.backend.service

import com.blaybus.backend.dto.CommentOnTaskRequest
import com.blaybus.backend.repository.TaskRepository
import com.blaybus.backend.repository.UserRepository
import com.blaybus.backend.repository.getByTaskId
import com.blaybus.backend.repository.getByUserId
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class TaskService(
    private val taskRepository: TaskRepository,
    private val userRepository: UserRepository,
) {
    @Transactional
    fun updateComment(menteeId: Long, taskId: Long, request: CommentOnTaskRequest) {
        val mentee = userRepository.getByUserId(menteeId)
        val task = taskRepository.getByTaskId(taskId)
        task.dailyPlanner.user.validateSameUser(mentee)
        task.updateComment(request.comment)
    }
}
