package com.blaybus.backend.service

import com.blaybus.backend.dto.AssignmentResponse
import com.blaybus.backend.dto.CommentOnTaskRequest
import com.blaybus.backend.dto.DailyAchievementRate
import com.blaybus.backend.dto.FeedbackDetail
import com.blaybus.backend.dto.FileUploadResponse
import com.blaybus.backend.dto.MenteeStudyTimeUpdateRequest
import com.blaybus.backend.dto.MenteeTaskCompletionUpdateRequest
import com.blaybus.backend.dto.MenteeTaskCreateRequest
import com.blaybus.backend.dto.MenteeTaskFeedbackResponse
import com.blaybus.backend.dto.MenteeTaskUpdateRequest
import com.blaybus.backend.dto.MentorTaskAssignRequest
import com.blaybus.backend.dto.MentorTaskUpdateRequest
import com.blaybus.backend.dto.PagedResponse
import com.blaybus.backend.dto.SliceResponse
import com.blaybus.backend.dto.TaskAndAssignmentResponse
import com.blaybus.backend.dto.TaskDetail
import com.blaybus.backend.dto.TaskDetailResponse
import com.blaybus.backend.dto.TaskImageResponse
import com.blaybus.backend.dto.TaskResponse
import com.blaybus.backend.entity.Assignment
import com.blaybus.backend.entity.StudyImage
import com.blaybus.backend.entity.Subject
import com.blaybus.backend.entity.Task
import com.blaybus.backend.entity.User
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
import com.blaybus.backend.util.getWeekRange
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
        files: List<MultipartFile>?,
    ): TaskResponse {
        val mentee = userRepository.getByUserId(userId)

        return createAndSaveTask(
            writer = mentee,
            targetMentee = mentee,
            date = request.date,
            title = request.title,
            content = request.content,
            subject = request.subject,
            files = files,
        )
    }

    @Transactional
    fun assignTask(
        mentorId: Long,
        request: MentorTaskAssignRequest,
        files: List<MultipartFile>?,
    ): TaskResponse {
        val mentor = userRepository.getByUserId(mentorId)
        val mentee = userRepository.getByUserId(request.menteeId)
        mentor.validateMentee(mentee)

        return createAndSaveTask(
            writer = mentor,
            targetMentee = mentee,
            date = request.date,
            title = request.title,
            content = request.content,
            subject = request.subject,
            files = files,
        )
    }

    @Transactional
    fun updateTask(
        userId: Long,
        taskId: Long,
        request: MenteeTaskUpdateRequest,
    ): TaskResponse {
        val task = taskRepository.getByTaskId(taskId)

        if (task.writer.id != userId && task.dailyPlanner.user.id != userId) {
            throw CustomException(ErrorCode.NOT_YOUR_TASK)
        }

        if (task.writer.id == userId) {
            task.title = request.title ?: task.title
            task.content = request.content ?: task.content
            task.subject = request.subject ?: task.subject
            task.studyDurationInMinutes = request.studyTime ?: task.studyDurationInMinutes
            task.isCompleted = request.isCompleted ?: task.isCompleted
        } else {
            if (request.studyTime != null) {
                task.studyDurationInMinutes = request.studyTime
            }
        }

        return TaskResponse.from(task)
    }

    @Transactional
    fun updateAssignedTask(
        userId: Long,
        taskId: Long,
        request: MentorTaskUpdateRequest,
        files: List<MultipartFile>?,
    ): TaskResponse {
        val task = taskRepository.getByTaskId(taskId)

        // 권한 검증: 작성자(멘토) 본인만 수정 가능
        if (task.writer.id != userId) {
            throw CustomException(ErrorCode.NOT_YOUR_TASK)
        }

        task.title = request.title ?: task.title
        task.content = request.content ?: task.content
        task.subject = request.subject ?: task.subject

        if (!files.isNullOrEmpty()) {
            manageAssignmentFiles(task, files)
        }

        return TaskResponse.from(task)
    }

    @Transactional
    fun updateStudyTime(
        userId: Long,
        taskId: Long,
        request: MenteeStudyTimeUpdateRequest,
    ): TaskResponse {
        val task = taskRepository.getByTaskId(taskId)

        if (task.dailyPlanner.user.id != userId) {
            throw CustomException(ErrorCode.NOT_YOUR_TASK)
        }

        task.updateStudyDurationInMinutes(request.studyTime)

        return TaskResponse.from(task)
    }

    @Transactional
    fun updateTaskCompletion(
        userId: Long,
        taskId: Long,
        request: MenteeTaskCompletionUpdateRequest,
    ): TaskResponse {
        val task = taskRepository.getByTaskId(taskId)

        if (task.writer.id != userId) {
            throw CustomException(ErrorCode.NOT_YOUR_TASK)
        }

        task.updateCompletionStatus(request.isCompleted)

        return TaskResponse.from(task)
    }

    @Transactional
    fun deleteTask(
        userId: Long,
        taskId: Long,
    ) {
        val task = taskRepository.getByTaskId(taskId)
        if (task.writer.id != userId) throw CustomException(ErrorCode.NOT_YOUR_TASK)
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
    fun getWeeklyAchievement(
        userId: Long,
        date: LocalDate,
    ): List<DailyAchievementRate> {
        userRepository.getByUserId(userId)
        val (startOfWeek, endOfWeek) = getWeekRange(date)
        val dailyPlannerList = dailyPlannerService.getDailyPlannerByPeriod(userId, startOfWeek, endOfWeek)
        var day = startOfWeek
        val weeklyAchievementList: MutableList<DailyAchievementRate> = mutableListOf()
        for (dailyPlanner in dailyPlannerList) {
            val tasks = dailyPlanner.tasks
            while (day.isBefore(dailyPlanner.date)) {
                weeklyAchievementList.add(DailyAchievementRate(day, 0, 0))
                day = day.plusDays(1)
            }
            weeklyAchievementList.add(
                DailyAchievementRate(
                    day,
                    completedTasks = tasks.count { it.isCompleted },
                    totalTasks = tasks.size,
                ),
            )
            day = day.plusDays(1)
        }
        return weeklyAchievementList
    }

    fun getTodayTasksForUser(
        user: User,
        date: LocalDate,
    ): List<Task> {
        val dailyPlanner = dailyPlannerService.getDailyPlannerOrNullByUserAndDate(user, date)
        return dailyPlanner?.tasks ?: emptyList()
    }

    @Transactional(readOnly = true)
    fun getTasksByDateList(
        userId: Long,
        date: LocalDate,
        lastId: Long?,
        size: Int,
    ): SliceResponse<TaskAndAssignmentResponse> {
        val user = userRepository.getByUserId(userId)
        val dailyPlanner =
            dailyPlannerService.getDailyPlannerOrNullByUserAndDate(user, date) ?: return SliceResponse(
                emptyList(),
                false,
            )
        val pageable = Pageable.ofSize(size)
        val tasks = taskRepository.findByDailyPlannerIdWithSlice(dailyPlanner.id, lastId, pageable)
        return SliceResponse(
            tasks.content.map { task ->
                val assignmentList =
                    assignmentRepository.findAllByTask(task).map {
                        AssignmentResponse(
                            it.id,
                            it.originalFileName,
                            objectStorageRepository.getDownloadUrl(it.fileKey),
                        )
                    }
                TaskAndAssignmentResponse(task, assignmentList, task.writer.id == user.id)
            },
            tasks.hasNext(),
        )
    }

    @Transactional(readOnly = true)
    fun getTaskByTaskId(
        userId: Long,
        taskId: Long,
    ): TaskDetailResponse {
        val task = taskRepository.getTaskAndDailyPlannerById(taskId)
        if (task.dailyPlanner.user.id != userId) {
            throw CustomException(ErrorCode.NOT_YOUR_TASK)
        }

        return TaskDetailResponse(task)
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

    private fun createAndSaveTask(
        writer: User,
        targetMentee: User,
        date: LocalDate,
        title: String,
        content: String?,
        subject: Subject,
        files: List<MultipartFile>?,
    ): TaskResponse {
        val planner = dailyPlannerService.getOrCreateDailyPlannerByDate(targetMentee, date)

        val task =
            taskRepository.save(
                Task(
                    dailyPlanner = planner,
                    subject = subject,
                    title = title,
                    content = content,
                    writer = writer,
                    isCompleted = false,
                ),
            )

        if (!files.isNullOrEmpty()) {
            manageAssignmentFiles(task, files)
        }

        return TaskResponse.from(task)
    }

    private fun manageAssignmentFiles(
        task: Task,
        files: List<MultipartFile>,
    ) {
        val existingAssignments = assignmentRepository.findAllByTask(task)

        if (existingAssignments.isNotEmpty()) {
            assignmentRepository.deleteAll(existingAssignments)
            assignmentRepository.flush() // 즉시 반영
        }

        // 2. 새 파일들 반복 업로드 및 저장
        files.forEach { file ->
            val filePath = "tasks/${task.id}/assignments/"
            val uploadedKey = objectStorageRepository.upload(filePath, file)

            assignmentRepository.save(
                Assignment(
                    task = task,
                    fileKey = uploadedKey,
                    originalFileName = file.originalFilename ?: "unknown.pdf",
                ),
            )
        }
    }
}
