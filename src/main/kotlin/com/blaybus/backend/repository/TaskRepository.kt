package com.blaybus.backend.repository

import com.blaybus.backend.entity.Task
import com.blaybus.backend.entity.User
import com.blaybus.backend.exception.CustomException
import com.blaybus.backend.exception.ErrorCode
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

fun TaskRepository.getByTaskId(taskId: Long): Task = findById(taskId).orElseThrow { CustomException(ErrorCode.TASK_NOT_FOUND) }

@Repository
interface TaskRepository : JpaRepository<Task, Long> {
    // 특정 작성자(멘티)가 쓴 Task 목록 페이징 조회
    @EntityGraph(attributePaths = ["studyImages", "feedback"])
    fun findByDailyPlannerUser(user: User, pageable: Pageable): Page<Task>
}
