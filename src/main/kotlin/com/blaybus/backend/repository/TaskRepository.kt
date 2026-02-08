package com.blaybus.backend.repository

import com.blaybus.backend.entity.Feedback
import com.blaybus.backend.entity.Task
import com.blaybus.backend.entity.User
import com.blaybus.backend.exception.CustomException
import com.blaybus.backend.exception.ErrorCode
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

fun TaskRepository.getByTaskId(taskId: Long): Task = findById(taskId).orElseThrow { CustomException(ErrorCode.TASK_NOT_FOUND) }

fun TaskRepository.getTaskAndDailyPlannerById(taskId: Long): Task = findTaskById(taskId) ?: throw CustomException(ErrorCode.TASK_NOT_FOUND)

@Repository
interface TaskRepository : JpaRepository<Task, Long> {
    @Query(
        """
    select f
    from Feedback f
    join fetch f.task t
    join fetch t.dailyPlanner dp
    where dp.user.id = :userId
      and t.createdDateTime >= :start
      and t.createdDateTime < :end
    """,
    )
    fun findByUserIdAndTaskCreatedBetween(
        userId: Long,
        start: LocalDateTime,
        end: LocalDateTime,
    ): List<Feedback>

    // 특정 작성자(멘티)가 쓴 Task 목록 페이징 조회
    @EntityGraph(attributePaths = ["studyImages", "feedback"])
    fun findByDailyPlannerUser(
        user: User,
        pageable: Pageable,
    ): Page<Task>

    @EntityGraph(attributePaths = ["dailyPlanner", "studyImages", "dailyPlanner.user"])
    fun findTaskById(taskId: Long): Task?

    @Query("""select t from Task t where t.dailyPlanner.id = :plannerId and (:lastId is null or t.id < :lastId) order by t.id desc""")
    fun sliceByDailyPlannerId(
        plannerId: Long,
        lastId: Long?,
        pageable: Pageable,
    ): Slice<Task>

    @EntityGraph(attributePaths = ["dailyPlanner"])
    fun findTaskById(id: Long): Task?
}
