package com.blaybus.backend.repository

import com.blaybus.backend.entity.Feedback
import com.blaybus.backend.entity.Task
import com.blaybus.backend.exception.CustomException
import com.blaybus.backend.exception.ErrorCode
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

fun TaskRepository.getByTaskId(taskId: Long): Task =
    findById(taskId).orElseThrow { CustomException(ErrorCode.TASK_NOT_FOUND) }

@Repository
interface TaskRepository : JpaRepository<Task, Long> {
    @Query("""
    select f
    from Feedback f
    join fetch f.task t
    join fetch t.dailyPlanner dp
    where dp.user.id = :userId
      and t.createdDateTime >= :start
      and t.createdDateTime < :end
    """)
    fun findByUserIdAndTaskCreatedBetween(
        userId: Long,
        start: LocalDateTime,
        end: LocalDateTime
    ): List<Feedback>

}
