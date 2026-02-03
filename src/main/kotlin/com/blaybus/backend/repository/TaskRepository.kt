package com.blaybus.backend.repository

import com.blaybus.backend.entity.Task
import com.blaybus.backend.exception.CustomException
import com.blaybus.backend.exception.ErrorCode
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

fun TaskRepository.getByTaskId(taskId: Long): Task = findById(taskId).orElseThrow { CustomException(ErrorCode.TASK_NOT_FOUND) }

@Repository
interface TaskRepository : JpaRepository<Task, Long> {

}
