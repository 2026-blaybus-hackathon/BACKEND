package com.blaybus.backend.service

import com.blaybus.backend.repository.TaskRepository
import org.springframework.stereotype.Service

@Service
class TaskService(
    private val taskRepository: TaskRepository,
) {
}
