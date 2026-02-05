package com.blaybus.backend.service

import com.blaybus.backend.dto.CommentOnTaskRequest
import com.blaybus.backend.dto.MenteeTaskCreateRequest
import com.blaybus.backend.dto.MenteeTaskUpdateRequest
import com.blaybus.backend.dto.MentorTaskAssignRequest
import com.blaybus.backend.dto.Subject
import com.blaybus.backend.entity.Assignment
import com.blaybus.backend.entity.DailyPlanner
import com.blaybus.backend.entity.Role
import com.blaybus.backend.entity.StudyImage
import com.blaybus.backend.entity.Task
import com.blaybus.backend.entity.User
import com.blaybus.backend.exception.CustomException
import com.blaybus.backend.exception.ErrorCode
import com.blaybus.backend.repository.AssignmentRepository
import com.blaybus.backend.repository.DailyPlannerRepository
import com.blaybus.backend.repository.ObjectStorageRepository
import com.blaybus.backend.repository.StudyImageRepository
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
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.mock.web.MockMultipartFile
import java.time.LocalDate
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class TaskServiceTest {
    @Mock
    lateinit var taskRepository: TaskRepository

    @Mock
    lateinit var userRepository: UserRepository

    @Mock
    lateinit var dailyPlannerRepository: DailyPlannerRepository

    @Mock
    lateinit var assignmentRepository: AssignmentRepository

    @Mock
    lateinit var studyImageRepository: StudyImageRepository

    @Mock
    lateinit var objectStorageRepository: ObjectStorageRepository

    @InjectMocks
    lateinit var taskService: TaskService

    private lateinit var mentor: User
    private lateinit var mentee: User
    private lateinit var otherUser: User
    private lateinit var dailyPlanner: DailyPlanner
    private lateinit var task: Task

    @BeforeEach
    fun setUp() {
        mentor =
            User(
                id = 1L,
                email = "mentor@test.com",
                name = "멘토",
                nickname = "mentor_nick",
                role = Role.MENTOR,
                password = "123",
            )

        mentee =
            User(
                id = 2L,
                email = "mentee@test.com",
                name = "멘티",
                nickname = "mentee_nick",
                role = Role.MENTEE,
                mentor = mentor,
                password = "123",
            )

        otherUser =
            User(
                id = 3L,
                email = "other@test.com",
                name = "다른 유저",
                nickname = "other_nick",
                role = Role.MENTEE,
                password = "123",
            )

        mentor.mentees.add(mentee)

        dailyPlanner =
            DailyPlanner(
                id = 1L,
                user = mentee,
                date = LocalDate.now(),
            )

        task =
            Task(
                id = 1L,
                dailyPlanner = dailyPlanner,
                subject = com.blaybus.backend.entity.Subject.KOREAN,
                title = "테스트 할 일",
                writer = mentee,
            )
    }

    @Nested
    @DisplayName("createTask 테스트")
    inner class CreateTaskTest {
        @Test
        @DisplayName("Task 생성 - 정상 플로우")
        fun `Task 생성 성공`() {
            // given
            val request =
                MenteeTaskCreateRequest(
                    title = "수학 공부",
                    content = "미적분 연습",
                    subject = com.blaybus.backend.entity.Subject.MATH,
                    date = LocalDate.now(),
                )

            whenever(userRepository.findById(mentee.id)).thenReturn(Optional.of(mentee))
            whenever(dailyPlannerRepository.findByUserAndDate(mentee, request.date)).thenReturn(dailyPlanner)
            whenever(taskRepository.save(any<Task>())).thenAnswer { it.arguments[0] }

            // when
            val result = taskService.createTask(mentee.id, request)

            // then
            assertThat(result.content).isEqualTo(request.title)
            assertThat(result.subject.name).isEqualTo(request.subject.name)
            verify(taskRepository).save(any<Task>())
        }

        @Test
        @DisplayName("Task 생성 - 플래너 없는 날짜")
        fun `플래너 없는 날짜에 Task 생성 시 플래너 자동 생성`() {
            // given
            val request =
                MenteeTaskCreateRequest(
                    title = "수학 공부",
                    content = "미적분 연습",
                    subject = com.blaybus.backend.entity.Subject.MATH,
                    date = LocalDate.of(2026, 2, 10),
                )

            val newPlanner =
                DailyPlanner(
                    id = 2L,
                    user = mentee,
                    date = request.date,
                    totalFeedback = "",
                )

            whenever(userRepository.findById(mentee.id)).thenReturn(Optional.of(mentee))
            whenever(dailyPlannerRepository.findByUserAndDate(mentee, request.date)).thenReturn(null)
            whenever(dailyPlannerRepository.save(any<DailyPlanner>())).thenReturn(newPlanner)
            whenever(taskRepository.save(any<Task>())).thenAnswer { it.arguments[0] }

            // when
            val result = taskService.createTask(mentee.id, request)

            // then
            verify(dailyPlannerRepository).save(any<DailyPlanner>())
            verify(taskRepository).save(any<Task>())
            assertThat(result.content).isEqualTo(request.title)
        }

        @Test
        @DisplayName("Task 생성 - 존재하지 않는 사용자")
        fun `존재하지 않는 사용자로 Task 생성 실패`() {
            // given
            val request =
                MenteeTaskCreateRequest(
                    title = "수학 공부",
                    content = "미적분 연습",
                    subject = com.blaybus.backend.entity.Subject.MATH,
                    date = LocalDate.now(),
                )

            whenever(userRepository.findById(99999L)).thenReturn(Optional.empty())

            // when & then
            assertThatThrownBy {
                taskService.createTask(99999L, request)
            }.isInstanceOf(CustomException::class.java)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND)
        }
    }

    @Nested
    @DisplayName("updateTask 테스트")
    inner class UpdateTaskTest {
        @Test
        @DisplayName("Task 수정 - 정상 플로우")
        fun `Task 수정 성공`() {
            // given
            val request =
                MenteeTaskUpdateRequest(
                    title = "수정된 제목",
                    content = "수정된 내용",
                    subject = Subject.KOREAN,
                    studyTime = 120,
                    isCompleted = true,
                )

            whenever(taskRepository.findById(task.id)).thenReturn(Optional.of(task))

            // when
            val result = taskService.updateTask(mentee.id, task.id, request)

            // then
            assertThat(task.title).isEqualTo(request.title)
            assertThat(task.content).isEqualTo(request.content)
            assertThat(task.studyDurationInMinutes).isEqualTo(request.studyTime)
            assertThat(task.isCompleted).isEqualTo(request.isCompleted)
            assertThat(result.content).isEqualTo(request.title)
        }

        @Test
        @DisplayName("Task 수정 - 다른 사용자 Task")
        fun `다른 사용자 Task 수정 시도 실패`() {
            // given
            val request =
                MenteeTaskUpdateRequest(
                    title = "수정된 제목",
                    content = "수정된 내용",
                    subject = Subject.KOREAN,
                    studyTime = 120,
                    isCompleted = true,
                )

            whenever(taskRepository.findById(task.id)).thenReturn(Optional.of(task))

            // when & then
            assertThatThrownBy {
                taskService.updateTask(otherUser.id, task.id, request)
            }.isInstanceOf(CustomException::class.java)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_YOUR_TASK)
        }

        @Test
        @DisplayName("Task 수정 - 존재하지 않는 Task")
        fun `존재하지 않는 Task 수정 실패`() {
            // given
            val request =
                MenteeTaskUpdateRequest(
                    title = "수정된 제목",
                    content = "수정된 내용",
                    subject = Subject.KOREAN,
                    studyTime = 120,
                    isCompleted = true,
                )

            whenever(taskRepository.findById(99999L)).thenReturn(Optional.empty())

            // when & then
            assertThatThrownBy {
                taskService.updateTask(mentee.id, 99999L, request)
            }.isInstanceOf(CustomException::class.java)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.TASK_NOT_FOUND)
        }
    }

    @Nested
    @DisplayName("deleteTask 테스트")
    inner class DeleteTaskTest {
        @Test
        @DisplayName("Task 삭제 - 정상 플로우")
        fun `Task 삭제 성공`() {
            // given
            whenever(taskRepository.findById(task.id)).thenReturn(Optional.of(task))

            // when
            taskService.deleteTask(mentee.id, task.id)

            // then
            verify(taskRepository).delete(task)
        }

        @Test
        @DisplayName("Task 삭제 - 다른 사용자 Task")
        fun `다른 사용자 Task 삭제 시도 실패`() {
            // given
            whenever(taskRepository.findById(task.id)).thenReturn(Optional.of(task))

            // when & then
            assertThatThrownBy {
                taskService.deleteTask(otherUser.id, task.id)
            }.isInstanceOf(CustomException::class.java)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_YOUR_TASK)
        }
    }

    @Nested
    @DisplayName("uploadVerificationImage 테스트")
    inner class UploadVerificationImageTest {
        @Test
        @DisplayName("인증 사진 업로드 - 정상 플로우")
        fun `인증 사진 업로드 성공`() {
            // given
            val mockImage =
                MockMultipartFile(
                    "image",
                    "study.jpg",
                    "image/jpeg",
                    ByteArray(1024),
                )

            val uploadedKey = "tasks/1/verification/study.jpg"
            val downloadUrl = "https://storage.example.com/$uploadedKey"

            val studyImage =
                StudyImage(
                    id = 1L,
                    task = task,
                    sequence = 1,
                    imageFileName = uploadedKey,
                    originalFileName = "study.jpg",
                )

            whenever(taskRepository.findById(task.id)).thenReturn(Optional.of(task))
            whenever(objectStorageRepository.upload(any(), eq(mockImage))).thenReturn(uploadedKey)
            whenever(objectStorageRepository.getDownloadUrl(uploadedKey)).thenReturn(downloadUrl)
            whenever(studyImageRepository.save(any<StudyImage>())).thenReturn(studyImage)

            // when
            val result = taskService.uploadVerificationImage(mentee.id, task.id, mockImage)

            // then
            assertThat(result.fileId).isEqualTo(studyImage.id)
            assertThat(result.url).isEqualTo(downloadUrl)
            assertThat(result.originalFilename).isEqualTo("study.jpg")
            verify(objectStorageRepository).upload(any(), eq(mockImage))
            verify(studyImageRepository).save(any<StudyImage>())
        }

        @Test
        @DisplayName("인증 사진 업로드 - 다른 사용자 Task")
        fun `다른 사용자 Task에 인증 사진 업로드 시도 실패`() {
            // given
            val mockImage =
                MockMultipartFile(
                    "image",
                    "study.jpg",
                    "image/jpeg",
                    ByteArray(1024),
                )

            whenever(taskRepository.findById(task.id)).thenReturn(Optional.of(task))

            // when & then
            assertThatThrownBy {
                taskService.uploadVerificationImage(otherUser.id, task.id, mockImage)
            }.isInstanceOf(CustomException::class.java)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_YOUR_TASK)
        }
    }

    @Nested
    @DisplayName("updateComment 테스트")
    inner class UpdateCommentTest {
        @Test
        @DisplayName("코멘트 업데이트 - 정상 플로우")
        fun `코멘트 작성 성공`() {
            // given
            val request = CommentOnTaskRequest(comment = "이 부분이 잘 이해되지 않습니다")

            whenever(userRepository.findById(mentee.id)).thenReturn(Optional.of(mentee))
            whenever(taskRepository.findById(task.id)).thenReturn(Optional.of(task))

            // when
            taskService.updateComment(mentee.id, task.id, request)

            // then
            assertThat(task.comment).isEqualTo(request.comment)
        }

        @Test
        @DisplayName("코멘트 업데이트 - 다른 사용자 Task")
        fun `다른 사용자 Task에 코멘트 작성 시도 실패`() {
            // given
            val request = CommentOnTaskRequest(comment = "테스트 코멘트")

            whenever(userRepository.findById(otherUser.id)).thenReturn(Optional.of(otherUser))
            whenever(taskRepository.findById(task.id)).thenReturn(Optional.of(task))

            // when & then
            assertThatThrownBy {
                taskService.updateComment(otherUser.id, task.id, request)
            }.isInstanceOf(CustomException::class.java)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_SAME_USER)
        }
    }

    @Nested
    @DisplayName("assignTask 테스트 (멘토 과제 할당)")
    inner class AssignTaskTest {
        @Test
        @DisplayName("과제 할당 - 정상 플로우 (PDF 포함)")
        fun `과제 할당 성공 - PDF 포함`() {
            // given
            val request =
                MentorTaskAssignRequest(
                    menteeId = mentee.id,
                    title = "과제: 미적분 문제",
                    content = "다음 문제를 풀어보세요",
                    subject = Subject.MATH,
                    date = LocalDate.now(),
                )

            val mockPdf =
                MockMultipartFile(
                    "file",
                    "assignment.pdf",
                    "application/pdf",
                    ByteArray(1024),
                )

            val uploadedKey = "tasks/1/assignments/assignment.pdf"

            whenever(userRepository.findById(mentor.id)).thenReturn(Optional.of(mentor))
            whenever(userRepository.findById(mentee.id)).thenReturn(Optional.of(mentee))
            whenever(dailyPlannerRepository.findByUserAndDate(mentee, request.date)).thenReturn(dailyPlanner)
            whenever(taskRepository.save(any<Task>())).thenAnswer { it.arguments[0] }
            whenever(objectStorageRepository.upload(any(), eq(mockPdf))).thenReturn(uploadedKey)
            whenever(assignmentRepository.save(any<Assignment>())).thenAnswer { it.arguments[0] }

            // when
            val result = taskService.assignTask(mentor.id, request, mockPdf)

            // then
            assertThat(result.content).isEqualTo(request.title)
            verify(taskRepository).save(any<Task>())
            verify(objectStorageRepository).upload(any(), eq(mockPdf))
            verify(assignmentRepository).save(any<Assignment>())
        }

        @Test
        @DisplayName("과제 할당 - PDF 없음")
        fun `과제 할당 성공 - PDF 없음`() {
            // given
            val request =
                MentorTaskAssignRequest(
                    menteeId = mentee.id,
                    title = "과제: 미적분 문제",
                    content = "다음 문제를 풀어보세요",
                    subject = Subject.MATH,
                    date = LocalDate.now(),
                )

            whenever(userRepository.findById(mentor.id)).thenReturn(Optional.of(mentor))
            whenever(userRepository.findById(mentee.id)).thenReturn(Optional.of(mentee))
            whenever(dailyPlannerRepository.findByUserAndDate(mentee, request.date)).thenReturn(dailyPlanner)
            whenever(taskRepository.save(any<Task>())).thenAnswer { it.arguments[0] }

            // when
            val result = taskService.assignTask(mentor.id, request, null)

            // then
            assertThat(result.content).isEqualTo(request.title)
            verify(taskRepository).save(any<Task>())
            verify(objectStorageRepository, never()).upload(any(), any())
            verify(assignmentRepository, never()).save(any<Assignment>())
        }

        @Test
        @DisplayName("과제 할당 - 자신의 멘티가 아닌 사용자")
        fun `자신의 멘티가 아닌 사용자에게 과제 할당 실패`() {
            // given
            val request =
                MentorTaskAssignRequest(
                    menteeId = otherUser.id,
                    title = "과제: 미적분 문제",
                    content = "다음 문제를 풀어보세요",
                    subject = Subject.MATH,
                    date = LocalDate.now(),
                )

            whenever(userRepository.findById(mentor.id)).thenReturn(Optional.of(mentor))
            whenever(userRepository.findById(otherUser.id)).thenReturn(Optional.of(otherUser))

            // when & then
            assertThatThrownBy {
                taskService.assignTask(mentor.id, request, null)
            }.isInstanceOf(CustomException::class.java)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_MY_MENTEE)
        }

        @Test
        @DisplayName("과제 할당 - 플래너 없는 날짜")
        fun `플래너 없는 날짜에 과제 할당 시 플래너 자동 생성`() {
            // given
            val request =
                MentorTaskAssignRequest(
                    menteeId = mentee.id,
                    title = "과제: 미적분 문제",
                    content = "다음 문제를 풀어보세요",
                    subject = Subject.MATH,
                    date = LocalDate.of(2026, 2, 15),
                )

            val newPlanner =
                DailyPlanner(
                    id = 3L,
                    user = mentee,
                    date = request.date,
                    totalFeedback = "",
                )

            whenever(userRepository.findById(mentor.id)).thenReturn(Optional.of(mentor))
            whenever(userRepository.findById(mentee.id)).thenReturn(Optional.of(mentee))
            whenever(dailyPlannerRepository.findByUserAndDate(mentee, request.date)).thenReturn(null)
            whenever(dailyPlannerRepository.save(any<DailyPlanner>())).thenReturn(newPlanner)
            whenever(taskRepository.save(any<Task>())).thenAnswer { it.arguments[0] }

            // when
            val result = taskService.assignTask(mentor.id, request, null)

            // then
            verify(dailyPlannerRepository).save(any<DailyPlanner>())
            verify(taskRepository).save(any<Task>())
            assertThat(result.content).isEqualTo(request.title)
        }
    }

    @Nested
    @DisplayName("getMenteeTasksWithFeedback 테스트")
    inner class GetMenteeTasksWithFeedbackTest {
        @Test
        @DisplayName("멘티 Task 조회 - 정상 플로우 (페이징)")
        fun `멘티 Task 및 피드백 목록 조회 성공`() {
            // given
            val pageable = PageRequest.of(0, 5)
            val tasks = listOf(task)
            val tasksPage = PageImpl(tasks, pageable, 1)

            whenever(userRepository.findById(mentor.id)).thenReturn(Optional.of(mentor))
            whenever(userRepository.findById(mentee.id)).thenReturn(Optional.of(mentee))
            whenever(taskRepository.findByDailyPlannerUser(mentee, pageable)).thenReturn(tasksPage)

            // when
            val result = taskService.getMenteeTasksWithFeedback(mentor.id, mentee.id, pageable)

            // then
            assertThat(result.menteeId).isEqualTo(mentee.id)
            assertThat(result.tasks.content).hasSize(1)
            assertThat(result.tasks.page).isEqualTo(0)
            assertThat(result.tasks.size).isEqualTo(5)
        }

        @Test
        @DisplayName("멘티 Task 조회 - 자신의 멘티가 아님")
        fun `자신의 멘티가 아닌 사용자 Task 조회 실패`() {
            // given
            val pageable = PageRequest.of(0, 5)

            whenever(userRepository.findById(mentor.id)).thenReturn(Optional.of(mentor))
            whenever(userRepository.findById(otherUser.id)).thenReturn(Optional.of(otherUser))

            // when & then
            assertThatThrownBy {
                taskService.getMenteeTasksWithFeedback(mentor.id, otherUser.id, pageable)
            }.isInstanceOf(CustomException::class.java)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_MY_MENTEE)
        }

        @Test
        @DisplayName("멘티 Task 조회 - Task 없음")
        fun `Task 없는 멘티 조회 시 빈 목록 반환`() {
            // given
            val pageable = PageRequest.of(0, 5)
            val tasksPage = PageImpl<Task>(emptyList(), pageable, 0)

            whenever(userRepository.findById(mentor.id)).thenReturn(Optional.of(mentor))
            whenever(userRepository.findById(mentee.id)).thenReturn(Optional.of(mentee))
            whenever(taskRepository.findByDailyPlannerUser(mentee, pageable)).thenReturn(tasksPage)

            // when
            val result = taskService.getMenteeTasksWithFeedback(mentor.id, mentee.id, pageable)

            // then
            assertThat(result.tasks.content).isEmpty()
            assertThat(result.tasks.totalElements).isEqualTo(0)
        }
    }
}
