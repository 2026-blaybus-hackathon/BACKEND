package com.blaybus.backend.service

import com.blaybus.backend.dto.FeedbackDto
import com.blaybus.backend.entity.DailyPlanner
import com.blaybus.backend.entity.Feedback
import com.blaybus.backend.entity.Provider
import com.blaybus.backend.entity.Subject
import com.blaybus.backend.entity.Task
import com.blaybus.backend.entity.User
import com.blaybus.backend.exception.CustomException
import com.blaybus.backend.exception.ErrorCode
import com.blaybus.backend.repository.DailyPlannerRepository
import com.blaybus.backend.repository.FeedbackRepository
import com.blaybus.backend.repository.TaskRepository
import com.blaybus.backend.repository.UserRepository
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class FeedbackServiceTest {

    @Mock
    lateinit var userRepository: UserRepository

    @Mock
    lateinit var feedbackRepository: FeedbackRepository

    @Mock
    lateinit var taskRepository: TaskRepository

    @Mock
    lateinit var dailyPlannerRepository: DailyPlannerRepository

    @InjectMocks
    lateinit var feedbackService: FeedbackService

    private lateinit var mentor: User
    private lateinit var mentee: User
    private lateinit var otherUser: User
    private lateinit var dailyPlanner: DailyPlanner
    private lateinit var task: Task

    @BeforeEach
    fun setUp() {
        mentor = User(
            id = 1L,
            email = "mentor@test.com",
            name = "멘토",
            nickname = "mentor_nick",
            provider = Provider.LOCAL,
            contactEmail = "mentor@test.com"
        )

        mentee = User(
            id = 2L,
            email = "mentee@test.com",
            name = "멘티",
            nickname = "mentee_nick",
            provider = Provider.LOCAL,
            contactEmail = "mentee@test.com",
            mentor = mentor
        )

        otherUser = User(
            id = 3L,
            email = "other@test.com",
            name = "다른 유저",
            nickname = "other_nick",
            provider = Provider.LOCAL,
            contactEmail = "other@test.com"
        )

        mentor.mentees.add(mentee)

        dailyPlanner = DailyPlanner(
            id = 1L,
            user = mentee,
            date = LocalDate.now()
        )

        task = Task(
            id = 1L,
            dailyPlanner = dailyPlanner,
            subject = Subject.KOREAN,
            title = "테스트 할 일",
            writer = mentee
        )
    }

    @Nested
    @DisplayName("provideFeedbackForMenteesTask 테스트")
    inner class ProvideFeedbackForMenteesTaskTest {

        @Test
        @DisplayName("멘토가 자신의 멘티 Task에 피드백을 작성하면 성공한다")
        fun `피드백 작성 성공`() {
            // given
            val request = FeedbackDto.CreateFeedbackRequest(
                summary = FeedbackDto.Summary(
                    keepContent = "잘한 점",
                    problemContent = "개선할 점",
                    tryContent = "시도할 점"
                ),
                content = "상세 피드백"
            )

            val savedFeedback = Feedback(
                id = 1L,
                task = task,
                mentor = mentor,
                keepContent = request.summary.keepContent,
                problemContent = request.summary.problemContent,
                tryContent = request.summary.tryContent,
                detail = request.content
            )

            whenever(userRepository.findById(mentor.id)).thenReturn(Optional.of(mentor))
            whenever(taskRepository.findById(task.id)).thenReturn(Optional.of(task))
            whenever(feedbackRepository.save(any<Feedback>())).thenReturn(savedFeedback)

            // when
            val result = feedbackService.provideFeedbackForMenteesTask(mentor.id, task.id, request)

            // then
            assertThat(result).isEqualTo(savedFeedback.id)
            verify(feedbackRepository).save(any<Feedback>())
        }

        @Test
        @DisplayName("멘토가 자신의 멘티가 아닌 사용자의 Task에 피드백을 작성하면 실패한다")
        fun `멘티가 아닌 사용자 Task에 피드백 작성 실패`() {
            // given
            val otherMenteeTask = Task(
                id = 2L,
                dailyPlanner = DailyPlanner(
                    id = 2L,
                    user = otherUser,
                    date = LocalDate.now()
                ),
                subject = Subject.MATH,
                title = "다른 유저의 할 일",
                writer = otherUser
            )

            val request = FeedbackDto.CreateFeedbackRequest(
                summary = FeedbackDto.Summary(
                    keepContent = "잘한 점",
                    problemContent = "개선할 점",
                    tryContent = "시도할 점"
                ),
                content = "상세 피드백"
            )

            whenever(userRepository.findById(mentor.id)).thenReturn(Optional.of(mentor))
            whenever(taskRepository.findById(otherMenteeTask.id)).thenReturn(Optional.of(otherMenteeTask))

            // when & then
            assertThatThrownBy {
                feedbackService.provideFeedbackForMenteesTask(mentor.id, otherMenteeTask.id, request)
            }
                .isInstanceOf(CustomException::class.java)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_MY_MENTEE)
        }

        @Test
        @DisplayName("존재하지 않는 사용자 ID로 피드백을 작성하면 실패한다")
        fun `존재하지 않는 사용자 피드백 작성 실패`() {
            // given
            val nonExistentUserId = 99999L
            val request = FeedbackDto.CreateFeedbackRequest(
                summary = FeedbackDto.Summary(
                    keepContent = "잘한 점",
                    problemContent = "개선할 점",
                    tryContent = "시도할 점"
                ),
                content = "상세 피드백"
            )

            whenever(userRepository.findById(nonExistentUserId)).thenReturn(Optional.empty())

            // when & then
            assertThatThrownBy {
                feedbackService.provideFeedbackForMenteesTask(nonExistentUserId, task.id, request)
            }
                .isInstanceOf(CustomException::class.java)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND)
        }

        @Test
        @DisplayName("존재하지 않는 Task ID로 피드백을 작성하면 실패한다")
        fun `존재하지 않는 Task 피드백 작성 실패`() {
            // given
            val nonExistentTaskId = 99999L
            val request = FeedbackDto.CreateFeedbackRequest(
                summary = FeedbackDto.Summary(
                    keepContent = "잘한 점",
                    problemContent = "개선할 점",
                    tryContent = "시도할 점"
                ),
                content = "상세 피드백"
            )

            whenever(userRepository.findById(mentor.id)).thenReturn(Optional.of(mentor))
            whenever(taskRepository.findById(nonExistentTaskId)).thenReturn(Optional.empty())

            // when & then
            assertThatThrownBy {
                feedbackService.provideFeedbackForMenteesTask(mentor.id, nonExistentTaskId, request)
            }
                .isInstanceOf(CustomException::class.java)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.TASK_NOT_FOUND)
        }
    }

    @Nested
    @DisplayName("provideTotalFeedbackForMenteesDailyPlanner 테스트")
    inner class ProvideTotalFeedbackForMenteesDailyPlannerTest {

        @Test
        @DisplayName("멘토가 자신의 멘티 플래너에 종합 피드백을 작성하면 성공한다")
        fun `종합 피드백 작성 성공`() {
            // given
            val request = FeedbackDto.CreateTotalFeedbackRequest(
                content = "오늘 하루 잘했습니다"
            )

            whenever(userRepository.findById(mentor.id)).thenReturn(Optional.of(mentor))
            whenever(dailyPlannerRepository.findById(dailyPlanner.id)).thenReturn(Optional.of(dailyPlanner))

            // when
            feedbackService.provideTotalFeedbackForMenteesDailyPlanner(mentor.id, dailyPlanner.id, request)

            // then
            assertThat(dailyPlanner.totalFeedback).isEqualTo(request.content)
        }

        @Test
        @DisplayName("멘토가 자신의 멘티가 아닌 사용자의 플래너에 종합 피드백을 작성하면 실패한다")
        fun `멘티가 아닌 사용자 플래너에 종합 피드백 작성 실패`() {
            // given
            val otherUserPlanner = DailyPlanner(
                id = 2L,
                user = otherUser,
                date = LocalDate.now()
            )

            val request = FeedbackDto.CreateTotalFeedbackRequest(
                content = "오늘 하루 잘했습니다"
            )

            whenever(userRepository.findById(mentor.id)).thenReturn(Optional.of(mentor))
            whenever(dailyPlannerRepository.findById(otherUserPlanner.id)).thenReturn(Optional.of(otherUserPlanner))

            // when & then
            assertThatThrownBy {
                feedbackService.provideTotalFeedbackForMenteesDailyPlanner(mentor.id, otherUserPlanner.id, request)
            }
                .isInstanceOf(CustomException::class.java)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_MY_MENTEE)
        }
    }

    @Nested
    @DisplayName("findFeedbackOfTask 테스트")
    inner class FindFeedbackOfTaskTest {

        @Test
        @DisplayName("멘티가 자신의 Task 피드백을 조회하면 성공한다")
        fun `피드백 조회 성공`() {
            // given
            val feedback = Feedback(
                id = 1L,
                task = task,
                mentor = mentor,
                keepContent = "잘한 점",
                problemContent = "개선할 점",
                tryContent = "시도할 점",
                detail = "상세 피드백"
            )
            task.feedback = feedback

            whenever(userRepository.findById(mentee.id)).thenReturn(Optional.of(mentee))
            whenever(taskRepository.findById(task.id)).thenReturn(Optional.of(task))

            // when
            val result = feedbackService.findFeedbackOfTask(mentee.id, task.id)

            // then
            assertThat(result.keepContent).isEqualTo("잘한 점")
            assertThat(result.problemContent).isEqualTo("개선할 점")
            assertThat(result.tryContent).isEqualTo("시도할 점")
            assertThat(result.detail).isEqualTo("상세 피드백")
        }

        @Test
        @DisplayName("피드백이 없는 Task를 조회하면 null 값들이 반환된다")
        fun `피드백 없는 Task 조회`() {
            // given
            task.feedback = null

            whenever(userRepository.findById(mentee.id)).thenReturn(Optional.of(mentee))
            whenever(taskRepository.findById(task.id)).thenReturn(Optional.of(task))

            // when
            val result = feedbackService.findFeedbackOfTask(mentee.id, task.id)

            // then
            assertThat(result.keepContent).isNull()
            assertThat(result.problemContent).isNull()
            assertThat(result.tryContent).isNull()
            assertThat(result.detail).isNull()
        }

        @Test
        @DisplayName("다른 사용자의 Task 피드백을 조회하면 실패한다")
        fun `다른 사용자 Task 피드백 조회 실패`() {
            // given
            whenever(userRepository.findById(otherUser.id)).thenReturn(Optional.of(otherUser))
            whenever(taskRepository.findById(task.id)).thenReturn(Optional.of(task))

            // when & then
            assertThatThrownBy {
                feedbackService.findFeedbackOfTask(otherUser.id, task.id)
            }
                .isInstanceOf(CustomException::class.java)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_SAME_USER)
        }
    }

    @Nested
    @DisplayName("findTotalFeedbackOfDailyPlanner 테스트")
    inner class FindTotalFeedbackOfDailyPlannerTest {

        @Test
        @DisplayName("멘티가 자신의 플래너 종합 피드백을 조회하면 성공한다")
        fun `종합 피드백 조회 성공`() {
            // given
            dailyPlanner.updateTotalFeedback("오늘 하루 잘했습니다")

            whenever(dailyPlannerRepository.findById(dailyPlanner.id)).thenReturn(Optional.of(dailyPlanner))
            whenever(userRepository.findById(mentee.id)).thenReturn(Optional.of(mentee))

            // when
            val result = feedbackService.findTotalFeedbackOfDailyPlanner(mentee.id, dailyPlanner.id)

            // then
            assertThat(result.totalFeedback).isEqualTo("오늘 하루 잘했습니다")
        }

        @Test
        @DisplayName("종합 피드백이 없는 플래너를 조회하면 null이 반환된다")
        fun `종합 피드백 없는 플래너 조회`() {
            // given
            whenever(dailyPlannerRepository.findById(dailyPlanner.id)).thenReturn(Optional.of(dailyPlanner))
            whenever(userRepository.findById(mentee.id)).thenReturn(Optional.of(mentee))

            // when
            val result = feedbackService.findTotalFeedbackOfDailyPlanner(mentee.id, dailyPlanner.id)

            // then
            assertThat(result.totalFeedback).isNull()
        }

        @Test
        @DisplayName("다른 사용자의 플래너 종합 피드백을 조회하면 실패한다")
        fun `다른 사용자 플래너 종합 피드백 조회 실패`() {
            // given
            whenever(dailyPlannerRepository.findById(dailyPlanner.id)).thenReturn(Optional.of(dailyPlanner))
            whenever(userRepository.findById(otherUser.id)).thenReturn(Optional.of(otherUser))

            // when & then
            assertThatThrownBy {
                feedbackService.findTotalFeedbackOfDailyPlanner(otherUser.id, dailyPlanner.id)
            }
                .isInstanceOf(CustomException::class.java)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_SAME_USER)
        }

        @Test
        @DisplayName("존재하지 않는 플래너 ID로 조회하면 실패한다")
        fun `존재하지 않는 플래너 조회 실패`() {
            // given
            val nonExistentPlannerId = 99999L

            whenever(dailyPlannerRepository.findById(nonExistentPlannerId)).thenReturn(Optional.empty())

            // when & then
            assertThatThrownBy {
                feedbackService.findTotalFeedbackOfDailyPlanner(mentee.id, nonExistentPlannerId)
            }
                .isInstanceOf(CustomException::class.java)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DAILY_PLANNER_NOT_FOUND)
        }
    }

    @Nested
    @DisplayName("findFeedbacksByMenteeId 테스트")
    inner class FindFeedbacksByMenteeIdTest {

        @Test
        @DisplayName("멘토가 자신의 멘티 피드백 목록을 조회하면 성공한다")
        fun `멘티 피드백 목록 조회 성공`() {
            // given
            val date = LocalDate.now()
            val feedback1 = Feedback(
                id = 1L,
                task = task,
                mentor = mentor,
                keepContent = "잘한 점 1",
                problemContent = "개선할 점 1",
                tryContent = "시도할 점 1",
                detail = "상세 피드백 1"
            )
            val task2 = Task(
                id = 2L,
                dailyPlanner = dailyPlanner,
                subject = Subject.MATH,
                title = "테스트 할 일 2",
                writer = mentee
            )
            val feedback2 = Feedback(
                id = 2L,
                task = task2,
                mentor = mentor,
                keepContent = "잘한 점 2",
                problemContent = "개선할 점 2",
                tryContent = "시도할 점 2",
                detail = "상세 피드백 2"
            )
            val feedbackList = listOf(feedback1, feedback2)

            whenever(userRepository.findById(mentor.id)).thenReturn(Optional.of(mentor))
            whenever(userRepository.findById(mentee.id)).thenReturn(Optional.of(mentee))
            whenever(taskRepository.findByUserIdAndTaskCreatedBetween(
                mentee.id,
                date.atStartOfDay(),
                date.plusDays(1).atStartOfDay()
            )).thenReturn(feedbackList)

            // when
            val result = feedbackService.findFeedbacksByMenteeId(mentee.id, mentor.id, date)

            // then
            assertThat(result).hasSize(2)
            assertThat(result[0].keepContent).isEqualTo("잘한 점 1")
            assertThat(result[1].keepContent).isEqualTo("잘한 점 2")
        }

        @Test
        @DisplayName("멘토가 자신의 멘티가 아닌 사용자의 피드백 목록을 조회하면 실패한다")
        fun `멘티가 아닌 사용자 피드백 목록 조회 실패`() {
            // given
            val date = LocalDate.now()

            whenever(userRepository.findById(mentor.id)).thenReturn(Optional.of(mentor))
            whenever(userRepository.findById(otherUser.id)).thenReturn(Optional.of(otherUser))

            // when & then
            assertThatThrownBy {
                feedbackService.findFeedbacksByMenteeId(otherUser.id, mentor.id, date)
            }
                .isInstanceOf(CustomException::class.java)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_MY_MENTEE)
        }

        @Test
        @DisplayName("피드백이 없는 날짜를 조회하면 빈 목록이 반환된다")
        fun `피드백 없는 날짜 조회`() {
            // given
            val date = LocalDate.now()

            whenever(userRepository.findById(mentor.id)).thenReturn(Optional.of(mentor))
            whenever(userRepository.findById(mentee.id)).thenReturn(Optional.of(mentee))
            whenever(taskRepository.findByUserIdAndTaskCreatedBetween(
                mentee.id,
                date.atStartOfDay(),
                date.plusDays(1).atStartOfDay()
            )).thenReturn(emptyList())

            // when
            val result = feedbackService.findFeedbacksByMenteeId(mentee.id, mentor.id, date)

            // then
            assertThat(result).isEmpty()
        }

        @Test
        @DisplayName("존재하지 않는 멘토 ID로 조회하면 실패한다")
        fun `존재하지 않는 멘토 조회 실패`() {
            // given
            val nonExistentMentorId = 99999L
            val date = LocalDate.now()

            whenever(userRepository.findById(nonExistentMentorId)).thenReturn(Optional.empty())

            // when & then
            assertThatThrownBy {
                feedbackService.findFeedbacksByMenteeId(mentee.id, nonExistentMentorId, date)
            }
                .isInstanceOf(CustomException::class.java)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND)
        }

        @Test
        @DisplayName("존재하지 않는 멘티 ID로 조회하면 실패한다")
        fun `존재하지 않는 멘티 조회 실패`() {
            // given
            val nonExistentMenteeId = 99999L
            val date = LocalDate.now()

            whenever(userRepository.findById(mentor.id)).thenReturn(Optional.of(mentor))
            whenever(userRepository.findById(nonExistentMenteeId)).thenReturn(Optional.empty())

            // when & then
            assertThatThrownBy {
                feedbackService.findFeedbacksByMenteeId(nonExistentMenteeId, mentor.id, date)
            }
                .isInstanceOf(CustomException::class.java)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND)
        }
    }

    @Nested
    @DisplayName("findMyFeedbacks 테스트")
    inner class FindMyFeedbacksTest {

        @Test
        @DisplayName("자신의 피드백 목록을 조회하면 성공한다")
        fun `내 피드백 목록 조회 성공`() {
            // given
            val date = LocalDate.now()
            val feedback1 = Feedback(
                id = 1L,
                task = task,
                mentor = mentor,
                keepContent = "잘한 점 1",
                problemContent = "개선할 점 1",
                tryContent = "시도할 점 1",
                detail = "상세 피드백 1"
            )
            val task2 = Task(
                id = 2L,
                dailyPlanner = dailyPlanner,
                subject = Subject.MATH,
                title = "테스트 할 일 2",
                writer = mentee
            )
            val feedback2 = Feedback(
                id = 2L,
                task = task2,
                mentor = mentor,
                keepContent = "잘한 점 2",
                problemContent = "개선할 점 2",
                tryContent = "시도할 점 2",
                detail = "상세 피드백 2"
            )
            val feedbackList = listOf(feedback1, feedback2)

            whenever(userRepository.findById(mentee.id)).thenReturn(Optional.of(mentee))
            whenever(taskRepository.findByUserIdAndTaskCreatedBetween(
                mentee.id,
                date.atStartOfDay(),
                date.plusDays(1).atStartOfDay()
            )).thenReturn(feedbackList)

            // when
            val result = feedbackService.findMyFeedbacks(mentee.id, date)

            // then
            assertThat(result).hasSize(2)
            assertThat(result[0].keepContent).isEqualTo("잘한 점 1")
            assertThat(result[1].keepContent).isEqualTo("잘한 점 2")
        }

        @Test
        @DisplayName("피드백이 없는 날짜를 조회하면 빈 목록이 반환된다")
        fun `피드백 없는 날짜 조회`() {
            // given
            val date = LocalDate.now()

            whenever(userRepository.findById(mentee.id)).thenReturn(Optional.of(mentee))
            whenever(taskRepository.findByUserIdAndTaskCreatedBetween(
                mentee.id,
                date.atStartOfDay(),
                date.plusDays(1).atStartOfDay()
            )).thenReturn(emptyList())

            // when
            val result = feedbackService.findMyFeedbacks(mentee.id, date)

            // then
            assertThat(result).isEmpty()
        }

        @Test
        @DisplayName("존재하지 않는 사용자 ID로 조회하면 실패한다")
        fun `존재하지 않는 사용자 조회 실패`() {
            // given
            val nonExistentUserId = 99999L
            val date = LocalDate.now()

            whenever(userRepository.findById(nonExistentUserId)).thenReturn(Optional.empty())

            // when & then
            assertThatThrownBy {
                feedbackService.findMyFeedbacks(nonExistentUserId, date)
            }
                .isInstanceOf(CustomException::class.java)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND)
        }
    }
}
