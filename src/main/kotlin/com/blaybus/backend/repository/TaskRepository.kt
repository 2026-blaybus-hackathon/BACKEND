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
import java.time.LocalDate
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

    @EntityGraph(attributePaths = ["studyImages", "feedback"])
    fun findByDailyPlannerUser(
        user: User,
        pageable: Pageable,
    ): Page<Task>

    @Query(
        """
        SELECT count(t), sum(CASE WHEN t.isCompleted = true THEN 1 ELSE 0 END)
        FROM Task t
        JOIN t.dailyPlanner dp
        JOIN dp.user u WHERE u.mentor.id = :mentorId AND dp.date BETWEEN :startDate AND :endDate
    """,
    )
    fun getTaskStatisticsByPeriod(
        mentorId: Long,
        startDate: LocalDate,
        endDate: LocalDate,
    ): List<Array<Any>>

    @Query(
        """
        SELECT COUNT(t) FROM Task t
        JOIN t.dailyPlanner dp
        JOIN dp.user u
        WHERE u.mentor.id = :mentorId
          AND t.isCompleted = true
          AND t.feedback IS NULL
    """,
    )
    fun countPendingFeedbackByMentorId(mentorId: Long): Int

    @Query(
        """
        SELECT t FROM Task t
        JOIN FETCH t.dailyPlanner dp
        JOIN FETCH dp.user u
        WHERE u.mentor.id = :mentorId
          AND t.isCompleted = true 
        ORDER BY t.createdDateTime DESC
    """,
    )
    fun findRecentSubmittedTasks(
        mentorId: Long,
        pageable: Pageable,
    ): List<Task>

    @EntityGraph(attributePaths = ["dailyPlanner", "studyImages", "dailyPlanner.user"])
    fun findTaskById(taskId: Long): Task?

    @Query("""select t from Task t where t.dailyPlanner.id = :plannerId and (:lastId is null or t.id < :lastId) order by t.id desc""")
    fun sliceByDailyPlannerId(
        plannerId: Long,
        lastId: Long?,
        pageable: Pageable,
    ): Slice<Task>

    @Query(
        """SELECT COUNT(t) > 0 FROM Task t WHERE t.dailyPlanner.user.id = :menteeId AND t.writer.id = :mentorId AND t.isCompleted = true AND t.feedback IS NULL""",
    )
    fun existsCompletedMentorTaskWithoutFeedback(
        menteeId: Long,
        mentorId: Long,
    ): Boolean

    @Query(
        """
        SELECT t
        FROM Task t
        JOIN t.dailyPlanner dp
        JOIN dp.user u
        WHERE u.id = :menteeId AND t.writer.id != :menteeId
          AND dp.date BETWEEN :startDate AND :endDate
    """,
    )
    fun reportSubjectByMenteeIdAndDateBetween(
        menteeId: Long,
        startDate: LocalDate,
        endDate: LocalDate,
    ): List<Task>
}
