package com.blaybus.backend.service

import com.blaybus.backend.dto.CommentOnTaskRequest
import com.blaybus.backend.dto.FeedbackDetail
import com.blaybus.backend.dto.FileUploadResponse
import com.blaybus.backend.dto.MenteeTaskCreateRequest
import com.blaybus.backend.dto.MenteeTaskFeedbackResponse
import com.blaybus.backend.dto.MenteeTaskUpdateRequest
import com.blaybus.backend.dto.MentorTaskAssignRequest
import com.blaybus.backend.dto.PagedResponse
import com.blaybus.backend.dto.SliceResponse
import com.blaybus.backend.dto.TaskDetail
import com.blaybus.backend.dto.TaskDetailResponse
import com.blaybus.backend.dto.TaskImageResponse
import com.blaybus.backend.dto.TaskResponse
import com.blaybus.backend.entity.Assignment
import com.blaybus.backend.entity.StudyImage
import com.blaybus.backend.entity.Task
import com.blaybus.backend.exception.CustomException
import com.blaybus.backend.exception.ErrorCode
import com.blaybus.backend.repository.AssignmentRepository
import com.blaybus.backend.repository.ObjectStorageRepository
import com.blaybus.backend.repository.StudyImageRepository
import com.blaybus.backend.repository.TaskRepository
import com.blaybus.backend.repository.UserRepository
import com.blaybus.backend.repository.getByTaskId
import com.blaybus.backend.repository.getByUserId
import com.blaybus.backend.repository.getTaskAndDailyPlannerById
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDate

@Service
@Transactional(readOnly = true)
class TaskService(
    private val taskRepository: TaskRepository,
    private val userRepository: UserRepository,
    private val dailyPlannerService: DailyPlannerService,
    private val assignmentRepository: AssignmentRepository,
    private val studyImageRepository: StudyImageRepository,
    private val objectStorageRepository: ObjectStorageRepository,
) {
    @Transactional
    fun updateComment(
        menteeId: Long,
        taskId: Long,
        request: CommentOnTaskRequest,
    ) {
        val mentee = userRepository.getByUserId(menteeId)
        val task = taskRepository.getByTaskId(taskId)
        task.dailyPlanner.user.validateSameUser(mentee)
        task.updateComment(request.comment)
    }

    // ================== 멘티 기능 (Task CRUD) ==================

    @Transactional
    fun createTask(
        userId: Long,
        request: MenteeTaskCreateRequest,
    ): TaskResponse {
        val user = userRepository.getByUserId(userId)

        // 에러 해결: DailyPlanner 생성 시 totalFeedback(null 가능) 명시
        val planner = dailyPlannerService.getOrCreateDailyPlannerByDate(user, request.date)

        val task =
            taskRepository.save(
                Task(
                    dailyPlanner = planner,
                    subject = request.subject,
                    title = request.title,
                    content = request.content,
                    writer = user,
                    isCompleted = false,
                ),
            )
        return TaskResponse(task)
    }

    @Transactional
    fun updateTask(
        userId: Long,
        taskId: Long,
        request: MenteeTaskUpdateRequest,
    ): TaskResponse {
        val task = taskRepository.getTaskAndDailyPlannerById(taskId)

        if (task.dailyPlanner.user.id != userId) throw CustomException(ErrorCode.NOT_YOUR_TASK) // 멘토가 과제 줄 경우 writer와 user가 다를 수 있음

        task.title = request.title
        task.content = request.content
        task.studyDurationInMinutes = request.studyTime // studyTime으로 매핑
        task.isCompleted = request.isCompleted ?: false
        return TaskResponse(task)
    }

    @Transactional
    fun deleteTask(
        userId: Long,
        taskId: Long,
    ) {
        val task = taskRepository.getTaskAndDailyPlannerById(taskId)
        if (task.dailyPlanner.user.id != userId) throw CustomException(ErrorCode.NOT_YOUR_TASK)
        taskRepository.delete(task)
    }

    @Transactional
    fun uploadVerificationImage(
        userId: Long,
        taskId: Long,
        image: MultipartFile,
    ): FileUploadResponse {
        val task = taskRepository.getTaskAndDailyPlannerById(taskId)
        if (task.dailyPlanner.user.id != userId) throw CustomException(ErrorCode.NOT_YOUR_TASK)

        val imagePath = "tasks/$taskId/verification/"
        val uploadedKey = objectStorageRepository.upload(imagePath, image)
        val downloadUrl = objectStorageRepository.getDownloadUrl(uploadedKey)

        val studyImage =
            studyImageRepository.save(
                StudyImage(
                    task = task,
                    sequence = task.studyImages.size + 1,
                    imageFileName = uploadedKey,
                    originalFileName = image.originalFilename ?: "unknown",
                ),
            )

        return FileUploadResponse(
            fileId = studyImage.id,
            url = downloadUrl,
            originalFilename = studyImage.originalFileName,
        )
    }

    @Transactional(readOnly = true)
    fun getTasksByDateList(
        userId: Long,
        date: LocalDate,
        lastId: Long?,
        size: Int,
    ): SliceResponse<TaskResponse> {
        val user = userRepository.getByUserId(userId)
        val dailyPlanner =
            dailyPlannerService.getDailyPlannerOrNullByUserAndDate(user, date) ?: return SliceResponse(
                emptyList(),
                false,
            )
        val pageable = Pageable.ofSize(size)
        val tasks = taskRepository.findByDailyPlannerIdWithSlice(dailyPlanner.id, lastId, pageable)
        return SliceResponse(tasks.content.map { task -> TaskResponse(task) }, tasks.hasNext())
    }

    @Transactional(readOnly = true)
    fun getTaskByTaskId(
        userId: Long,
        taskId: Long,
    ): TaskDetailResponse {
        val user = userRepository.getByUserId(userId)
        val task = taskRepository.getTaskAndDailyPlannerById(taskId)
        user.validateSameUser(task.dailyPlanner.user)

        return TaskDetailResponse(task)
    }
    // ================== 멘토 기능 (과제 할당 및 조회) ==================

    @Transactional
    fun assignTask(
        mentorId: Long,
        request: MentorTaskAssignRequest,
        file: MultipartFile?,
    ): TaskResponse {
        val mentor = userRepository.getByUserId(mentorId)
        val mentee = userRepository.getByUserId(request.menteeId)

        mentor.validateMentee(mentee)

        val planner = dailyPlannerService.getOrCreateDailyPlannerByDate(mentee, request.date)
        val task =
            taskRepository.save(
                Task(
                    dailyPlanner = planner,
                    subject = request.subject,
                    title = request.title,
                    content = request.content,
                    writer = mentor,
                    isCompleted = false,
                ),
            )

        file?.let {
            val filePath = "tasks/${task.id}/assignments/"
            val uploadedKey = objectStorageRepository.upload(filePath, it)
            assignmentRepository.save(Assignment(task = task, pdfFileName = uploadedKey))
        }
        return TaskResponse(task)
    }

    fun getMenteeTasksWithFeedback(
        mentorId: Long,
        menteeId: Long,
        pageable: Pageable,
    ): MenteeTaskFeedbackResponse {
        val mentor = userRepository.getByUserId(mentorId)
        val mentee = userRepository.getByUserId(menteeId)
        mentor.validateMentee(mentee)

        val tasksPage = taskRepository.findByDailyPlannerUser(mentee, pageable)

        val taskDetails =
            tasksPage.content.map { task ->
                TaskDetail(
                    taskId = task.id,
                    title = task.title,
                    images =
                        task.studyImages.map { img ->
                            TaskImageResponse(
                                url = objectStorageRepository.getDownloadUrl(img.imageFileName),
                                name = img.originalFileName,
                                sequence = img.sequence,
                            )
                        },
                    feedback =
                        task.feedback?.let { fb ->
                            FeedbackDetail(
                                feedbackId = fb.id,
                                summary = "${fb.keepContent} / ${fb.problemContent}",
                                comment = fb.detail ?: "",
                            )
                        } ?: FeedbackDetail(0L, "No Feedback", ""),
                )
            }

        return MenteeTaskFeedbackResponse(
            menteeId = mentee.id,
            tasks =
                PagedResponse(
                    content = taskDetails,
                    page = tasksPage.number,
                    size = tasksPage.size,
                    totalPages = tasksPage.totalPages,
                    totalElements = tasksPage.totalElements,
                ),
        )
    }
}
