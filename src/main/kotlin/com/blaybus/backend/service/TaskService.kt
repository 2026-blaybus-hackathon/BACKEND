package com.blaybus.backend.service

import com.blaybus.backend.entity.Task
import com.blaybus.backend.repository.TaskRepository
import com.blaybus.backend.repository.getByTaskId
import org.springframework.stereotype.Service

@Service
class TaskService(
    private val taskRepository: TaskRepository,
) {
    fun findById(
        taskId: Long
    ): Task {
        return taskRepository.getByTaskId(taskId)
    }
}