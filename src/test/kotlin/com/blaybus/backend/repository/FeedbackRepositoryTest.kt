package com.blaybus.backend.repository

import com.blaybus.backend.entity.DailyPlanner
import com.blaybus.backend.entity.Feedback
import com.blaybus.backend.entity.Provider
import com.blaybus.backend.entity.Subject
import com.blaybus.backend.entity.Task
import com.blaybus.backend.entity.User
import com.blaybus.backend.exception.CustomException
import com.blaybus.backend.exception.ErrorCode
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
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
class FeedbackRepositoryTest {

    @Autowired
    lateinit var feedbackRepository: FeedbackRepository

    @Autowired
    lateinit var entityManager: TestEntityManager

    private lateinit var mentor: User
    private lateinit var mentee: User
    private lateinit var dailyPlanner: DailyPlanner
    private lateinit var task: Task

    @BeforeEach
    fun setUp() {
        mentor = User(
            email = "mentor@test.com",
            name = "mentor",
            nickname = "mentor_nick",
            provider = Provider.LOCAL,
            contactEmail = "mentor@test.com"
        )
        entityManager.persist(mentor)

        mentee = User(
            email = "mentee@test.com",
            name = "mentee",
            nickname = "mentee_nick",
            provider = Provider.LOCAL,
            contactEmail = "mentee@test.com",
            mentor = mentor
        )
        entityManager.persist(mentee)

        dailyPlanner = DailyPlanner(
            user = mentee,
            date = LocalDate.now()
        )
        entityManager.persist(dailyPlanner)

        task = Task(
            dailyPlanner = dailyPlanner,
            subject = Subject.KOREAN,
            title = "test task",
            writer = mentee
        )
        entityManager.persist(task)

        entityManager.flush()
        entityManager.clear()
    }

    @Nested
    @DisplayName("Save Feedback")
    inner class SaveFeedbackTest {

        @Test
        @DisplayName("success - save feedback generates ID")
        fun saveFeedbackSuccess() {
            // given
            val foundTask = entityManager.find(Task::class.java, task.id)!!
            val foundMentor = entityManager.find(User::class.java, mentor.id)!!
            val feedback = Feedback(
                task = foundTask,
                mentor = foundMentor,
                keepContent = "keep",
                problemContent = "problem",
                tryContent = "try",
                detail = "detail"
            )

            // when
            val savedFeedback = feedbackRepository.save(feedback)

            // then
            assertThat(savedFeedback.id).isNotNull()
            assertThat(savedFeedback.id).isGreaterThan(0)
        }

        @Test
        @DisplayName("success - save feedback with all fields")
        fun saveFeedbackAllFields() {
            // given
            val keepContent = "keep content"
            val problemContent = "problem content"
            val tryContent = "try content"
            val detail = "detail content"

            val foundTask = entityManager.find(Task::class.java, task.id)!!
            val foundMentor = entityManager.find(User::class.java, mentor.id)!!
            val feedback = Feedback(
                task = foundTask,
                mentor = foundMentor,
                keepContent = keepContent,
                problemContent = problemContent,
                tryContent = tryContent,
                detail = detail
            )

            // when
            val savedFeedback = feedbackRepository.save(feedback)
            entityManager.flush()
            entityManager.clear()

            // then
            val foundFeedback = feedbackRepository.findById(savedFeedback.id).orElseThrow()
            assertThat(foundFeedback.keepContent).isEqualTo(keepContent)
            assertThat(foundFeedback.problemContent).isEqualTo(problemContent)
            assertThat(foundFeedback.tryContent).isEqualTo(tryContent)
            assertThat(foundFeedback.detail).isEqualTo(detail)
        }

        @Test
        @DisplayName("success - save feedback with null detail")
        fun saveFeedbackNullDetail() {
            // given
            val foundTask = entityManager.find(Task::class.java, task.id)!!
            val foundMentor = entityManager.find(User::class.java, mentor.id)!!
            val feedback = Feedback(
                task = foundTask,
                mentor = foundMentor,
                keepContent = "keep",
                problemContent = "problem",
                tryContent = "try",
                detail = null
            )

            // when
            val savedFeedback = feedbackRepository.save(feedback)
            entityManager.flush()
            entityManager.clear()

            // then
            val foundFeedback = feedbackRepository.findById(savedFeedback.id).orElseThrow()
            assertThat(foundFeedback.detail).isNull()
        }
    }

    @Nested
    @DisplayName("getByFeedbackId extension function")
    inner class GetByFeedbackIdTest {

        @Test
        @DisplayName("success - get existing feedback by ID")
        fun getExistingFeedbackSuccess() {
            // given
            val foundTask = entityManager.find(Task::class.java, task.id)!!
            val foundMentor = entityManager.find(User::class.java, mentor.id)!!
            val feedback = Feedback(
                task = foundTask,
                mentor = foundMentor,
                keepContent = "keep",
                problemContent = "problem",
                tryContent = "try"
            )
            val savedFeedback = feedbackRepository.save(feedback)
            entityManager.flush()
            entityManager.clear()

            // when
            val foundFeedback = feedbackRepository.getByFeedbackId(savedFeedback.id)

            // then
            assertThat(foundFeedback.id).isEqualTo(savedFeedback.id)
            assertThat(foundFeedback.keepContent).isEqualTo("keep")
        }

        @Test
        @DisplayName("fail - get non-existing feedback throws CustomException")
        fun getNonExistingFeedbackFail() {
            // given
            val nonExistentId = 99999L

            // when & then
            assertThatThrownBy { feedbackRepository.getByFeedbackId(nonExistentId) }
                .isInstanceOf(CustomException::class.java)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FEEDBACK_NOT_FOUND)
        }
    }

    @Nested
    @DisplayName("Delete Feedback")
    inner class DeleteFeedbackTest {

        @Test
        @DisplayName("success - delete feedback")
        fun deleteFeedbackSuccess() {
            // given
            val foundTask = entityManager.find(Task::class.java, task.id)!!
            val foundMentor = entityManager.find(User::class.java, mentor.id)!!
            val feedback = Feedback(
                task = foundTask,
                mentor = foundMentor,
                keepContent = "keep",
                problemContent = "problem",
                tryContent = "try"
            )
            val savedFeedback = feedbackRepository.save(feedback)
            entityManager.flush()
            entityManager.clear()

            // when
            feedbackRepository.deleteById(savedFeedback.id)
            entityManager.flush()
            entityManager.clear()

            // then
            val result = feedbackRepository.findById(savedFeedback.id)
            assertThat(result).isEmpty()
        }
    }
}
