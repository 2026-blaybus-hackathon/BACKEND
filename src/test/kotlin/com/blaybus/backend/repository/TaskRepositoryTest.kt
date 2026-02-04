package com.blaybus.backend.repository

import com.blaybus.backend.entity.DailyPlanner
import com.blaybus.backend.entity.Feedback
import com.blaybus.backend.entity.Role
import com.blaybus.backend.entity.Subject
import com.blaybus.backend.entity.Task
import com.blaybus.backend.entity.User
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import java.time.LocalDate

@DataJpaTest
@EnableJpaAuditing
class TaskRepositoryTest {
    @Autowired
    lateinit var taskRepository: TaskRepository

    @Autowired
    lateinit var entityManager: TestEntityManager

    private lateinit var mentor: User
    private lateinit var mentee: User
    private lateinit var dailyPlanner: DailyPlanner

    @BeforeEach
    fun setUp() {
        mentor =
            User(
                email = "mentor@test.com",
                name = "mentor",
                nickname = "mentor_nick",
                role = Role.MENTOR,
                password = "123",
            )
        entityManager.persist(mentor)

        mentee =
            User(
                email = "mentee@test.com",
                name = "mentee",
                nickname = "mentee_nick",
                role = Role.MENTEE,
                mentor = mentor,
                password = "123",
            )
        entityManager.persist(mentee)

        dailyPlanner =
            DailyPlanner(
                user = mentee,
                date = LocalDate.now(),
            )
        entityManager.persist(dailyPlanner)

        entityManager.flush()
        entityManager.clear()
    }

    @Nested
    @DisplayName("findByUserIdAndTaskCreatedBetween")
    inner class FindByUserIdAndTaskCreatedBetweenTest {
        @Test
        @DisplayName("success - returns feedbacks for user within date range")
        fun findFeedbacksWithinDateRange() {
            // given
            val foundMentee = entityManager.find(User::class.java, mentee.id)!!
            val foundMentor = entityManager.find(User::class.java, mentor.id)!!
            val foundPlanner = entityManager.find(DailyPlanner::class.java, dailyPlanner.id)!!

            val task1 =
                Task(
                    dailyPlanner = foundPlanner,
                    subject = Subject.KOREAN,
                    title = "task 1",
                    writer = foundMentee,
                )
            entityManager.persist(task1)

            val task2 =
                Task(
                    dailyPlanner = foundPlanner,
                    subject = Subject.MATH,
                    title = "task 2",
                    writer = foundMentee,
                )
            entityManager.persist(task2)

            val feedback1 =
                Feedback(
                    task = task1,
                    mentor = foundMentor,
                    keepContent = "keep 1",
                    problemContent = "problem 1",
                    tryContent = "try 1",
                )
            entityManager.persist(feedback1)

            val feedback2 =
                Feedback(
                    task = task2,
                    mentor = foundMentor,
                    keepContent = "keep 2",
                    problemContent = "problem 2",
                    tryContent = "try 2",
                )
            entityManager.persist(feedback2)

            entityManager.flush()
            entityManager.clear()

            val today = LocalDate.now()
            val start = today.atStartOfDay()
            val end = today.plusDays(1).atStartOfDay()

            // when
            val result = taskRepository.findByUserIdAndTaskCreatedBetween(mentee.id, start, end)

            // then
            assertThat(result).hasSize(2)
            assertThat(result.map { it.keepContent }).containsExactlyInAnyOrder("keep 1", "keep 2")
        }

        @Test
        @DisplayName("success - returns empty list when no feedbacks exist")
        fun findNoFeedbacks() {
            // given
            val foundMentee = entityManager.find(User::class.java, mentee.id)!!
            val foundPlanner = entityManager.find(DailyPlanner::class.java, dailyPlanner.id)!!

            val task =
                Task(
                    dailyPlanner = foundPlanner,
                    subject = Subject.KOREAN,
                    title = "task without feedback",
                    writer = foundMentee,
                )
            entityManager.persist(task)

            entityManager.flush()
            entityManager.clear()

            val today = LocalDate.now()
            val start = today.atStartOfDay()
            val end = today.plusDays(1).atStartOfDay()

            // when
            val result = taskRepository.findByUserIdAndTaskCreatedBetween(mentee.id, start, end)

            // then
            assertThat(result).isEmpty()
        }

        @Test
        @DisplayName("success - returns empty list when date range has no tasks")
        fun findFeedbacksOutsideDateRange() {
            // given
            val foundMentee = entityManager.find(User::class.java, mentee.id)!!
            val foundMentor = entityManager.find(User::class.java, mentor.id)!!
            val foundPlanner = entityManager.find(DailyPlanner::class.java, dailyPlanner.id)!!

            val task =
                Task(
                    dailyPlanner = foundPlanner,
                    subject = Subject.KOREAN,
                    title = "task",
                    writer = foundMentee,
                )
            entityManager.persist(task)

            val feedback =
                Feedback(
                    task = task,
                    mentor = foundMentor,
                    keepContent = "keep",
                    problemContent = "problem",
                    tryContent = "try",
                )
            entityManager.persist(feedback)

            entityManager.flush()
            entityManager.clear()

            // Query for yesterday (when no tasks were created)
            val yesterday = LocalDate.now().minusDays(1)
            val start = yesterday.atStartOfDay()
            val end = yesterday.plusDays(1).atStartOfDay().minusNanos(1)

            // when
            val result = taskRepository.findByUserIdAndTaskCreatedBetween(mentee.id, start, end)

            // then
            assertThat(result).isEmpty()
        }

        @Test
        @DisplayName("success - returns only feedbacks for specified user")
        fun findFeedbacksOnlyForSpecifiedUser() {
            // given
            val foundMentee = entityManager.find(User::class.java, mentee.id)!!
            val foundMentor = entityManager.find(User::class.java, mentor.id)!!
            val foundPlanner = entityManager.find(DailyPlanner::class.java, dailyPlanner.id)!!

            // Create another user with their own planner and task
            val otherUser =
                User(
                    email = "other@test.com",
                    name = "other",
                    nickname = "other_nick",
                    role = Role.MENTEE,
                    password = "123",
                )
            entityManager.persist(otherUser)

            val otherPlanner =
                DailyPlanner(
                    user = otherUser,
                    date = LocalDate.now(),
                )
            entityManager.persist(otherPlanner)

            // Mentee's task and feedback
            val menteeTask =
                Task(
                    dailyPlanner = foundPlanner,
                    subject = Subject.KOREAN,
                    title = "mentee task",
                    writer = foundMentee,
                )
            entityManager.persist(menteeTask)

            val menteeFeedback =
                Feedback(
                    task = menteeTask,
                    mentor = foundMentor,
                    keepContent = "mentee keep",
                    problemContent = "mentee problem",
                    tryContent = "mentee try",
                )
            entityManager.persist(menteeFeedback)

            // Other user's task and feedback
            val otherTask =
                Task(
                    dailyPlanner = otherPlanner,
                    subject = Subject.MATH,
                    title = "other task",
                    writer = otherUser,
                )
            entityManager.persist(otherTask)

            val otherFeedback =
                Feedback(
                    task = otherTask,
                    mentor = foundMentor,
                    keepContent = "other keep",
                    problemContent = "other problem",
                    tryContent = "other try",
                )
            entityManager.persist(otherFeedback)

            entityManager.flush()
            entityManager.clear()

            val today = LocalDate.now()
            val start = today.atStartOfDay()
            val end = today.plusDays(1).atStartOfDay()

            // when
            val result = taskRepository.findByUserIdAndTaskCreatedBetween(mentee.id, start, end)

            // then
            assertThat(result).hasSize(1)
            assertThat(result[0].keepContent).isEqualTo("mentee keep")
        }
    }

    @Nested
    @DisplayName("getByTaskId extension function")
    inner class GetByTaskIdTest {
        @Test
        @DisplayName("success - get existing task by ID")
        fun getExistingTaskSuccess() {
            // given
            val foundMentee = entityManager.find(User::class.java, mentee.id)!!
            val foundPlanner = entityManager.find(DailyPlanner::class.java, dailyPlanner.id)!!

            val task =
                Task(
                    dailyPlanner = foundPlanner,
                    subject = Subject.KOREAN,
                    title = "test task",
                    writer = foundMentee,
                )
            entityManager.persist(task)
            entityManager.flush()
            entityManager.clear()

            // when
            val foundTask = taskRepository.getByTaskId(task.id)

            // then
            assertThat(foundTask.id).isEqualTo(task.id)
            assertThat(foundTask.title).isEqualTo("test task")
        }

        @Test
        @DisplayName("fail - get non-existing task throws CustomException")
        fun getNonExistingTaskFail() {
            // given
            val nonExistentId = 99999L

            // when & then
            org.assertj.core.api.Assertions
                .assertThatThrownBy {
                    taskRepository.getByTaskId(nonExistentId)
                }.isInstanceOf(com.blaybus.backend.exception.CustomException::class.java)
                .hasFieldOrPropertyWithValue("errorCode", com.blaybus.backend.exception.ErrorCode.TASK_NOT_FOUND)
        }
    }
}
