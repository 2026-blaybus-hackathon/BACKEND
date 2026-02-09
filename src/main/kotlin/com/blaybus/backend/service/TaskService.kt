package com.blaybus.backend.service

import com.blaybus.backend.dto.AssignmentResponse
import com.blaybus.backend.dto.CommentOnTaskRequest
import com.blaybus.backend.dto.DashboardStatsDto
import com.blaybus.backend.dto.FeedbackDetail
import com.blaybus.backend.dto.FileUploadResponse
import com.blaybus.backend.dto.MenteeStudyTimeUpdateRequest
import com.blaybus.backend.dto.MenteeSummaryDto
import com.blaybus.backend.dto.MenteeTaskCompletionUpdateRequest
import com.blaybus.backend.dto.MenteeTaskCreateRequest
import com.blaybus.backend.dto.MenteeTaskFeedbackResponse
import com.blaybus.backend.dto.MenteeTaskUpdateRequest
import com.blaybus.backend.dto.MentorDashboardResponse
import com.blaybus.backend.dto.MentorMyPageStatsDto
import com.blaybus.backend.dto.MentorTaskAssignRequest
import com.blaybus.backend.dto.MentorTaskUpdateRequest
import com.blaybus.backend.dto.PagedResponse
import com.blaybus.backend.dto.RecentTaskSummaryDto
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
import com.blaybus.backend.entity.TaskType
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
import com.blaybus.backend.util.getDDay
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

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
            taskType = request.taskType,
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
            taskType = request.taskType,
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
        task.updateCompletionStatus(true)
        return FileUploadResponse(
            fileId = studyImage.id,
            url = downloadUrl,
            originalFilename = studyImage.originalFileName,
        )
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
        val tasks = taskRepository.sliceByDailyPlannerId(dailyPlanner.id, lastId, pageable)
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
        taskType: TaskType,
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
                    taskType = taskType,
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

    fun getDashboardData(mentorId: Long): MentorDashboardResponse {
        // 1. 멘티 목록 조회
        val mentees = userRepository.findAllByMentorId(mentorId)
        val menteeDtos =
            mentees.map { mentee ->
                MenteeSummaryDto(
                    menteeId = mentee.id,
                    name = mentee.name,
                    school = mentee.schoolName ?: "-",
                    grade = mentee.grade?.description ?: "-",
                    profileImageUrl =
                        mentee.profileName?.let {
                            objectStorageRepository.getDownloadUrl(it)
                        },
                    dday = mentee.targetDate?.let { getDDay(it, LocalDate.now()) },
                )
            }

        // 2. 주간 진행률 통계 (지난주 대비 이번주 상승률)
        val today = LocalDate.now()
        val startThisWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val endThisWeek = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
        val startLastWeek = startThisWeek.minusWeeks(1)
        val endLastWeek = endThisWeek.minusWeeks(1)

        val thisWeekRate = calculateProgressRate(mentorId, startThisWeek, endThisWeek)
        val lastWeekRate = calculateProgressRate(mentorId, startLastWeek, endLastWeek)
        val progressChange = thisWeekRate - lastWeekRate

        // 3. 팬딩 피드백 수
        val pendingCount = taskRepository.countPendingFeedbackByMentorId(mentorId)

        // 4. 최근 제출 과제 (상위 5건)
        val recentTasks =
            taskRepository
                .findRecentSubmittedTasks(
                    mentorId,
                    PageRequest.of(0, 5),
                ).map { task ->
                    val u = task.dailyPlanner.user
                    RecentTaskSummaryDto(
                        taskId = task.id,
                        title = task.title,
                        menteeName = u.name,
                        completedAt = task.completedTime,
                        schoolAndGrade = "${u.schoolName ?: ""} ${u.grade?.description ?: ""}",
                        isFeedbackCompleted = task.feedback != null,
                        targetSchool = u.targetSchool ?: "",
                        completedTime = task.completedTime,
                    )
                }

        return MentorDashboardResponse(
            stats =
                DashboardStatsDto(
                    totalMenteeCount = mentees.size,
                    averageProgress = thisWeekRate,
                    progressChange = progressChange,
                    pendingFeedbackCount = pendingCount,
                ),
            mentees = menteeDtos,
            recentTasks = recentTasks,
        )
    }

    private fun calculateProgressRate(
        mentorId: Long,
        start: LocalDate,
        end: LocalDate,
    ): Int {
        val stats = taskRepository.getTaskStatisticsByPeriod(mentorId, start, end)
        if (stats.isEmpty()) return 0

        val row = stats[0]
        val totalTasks = (row[0] as Number).toLong()
        val completedTasks = (row[1] as? Number)?.toLong() ?: 0L

        return if (totalTasks == 0L) {
            0
        } else {
            ((completedTasks.toDouble() / totalTasks.toDouble()) * 100).toInt()
        }
    }

    fun getMentorMyPageStats(mentorId: Long): MentorMyPageStatsDto {
        userRepository.getByUserId(mentorId)

        val menteeCount = userRepository.countByMentorId(mentorId)

        val totalStudyMinutes = taskRepository.getTotalStudyTimeByMentorId(mentorId) ?: 0L
        val averageStudyTime = if (menteeCount > 0) (totalStudyMinutes / menteeCount).toInt() else 0

        val stats = taskRepository.getCompletionRateStatsByMentorId(mentorId)
        var completionRate = 0
        if (stats.isNotEmpty()) {
            val row = stats[0]
            val totalTasks = (row[0] as Number).toLong()
            val completedTasks = (row[1] as? Number)?.toLong() ?: 0L

            if (totalTasks > 0) {
                completionRate = ((completedTasks.toDouble() / totalTasks.toDouble()) * 100).toInt()
            }
        }

        return MentorMyPageStatsDto(
            totalMenteeCount = menteeCount,
            averageStudyTime = averageStudyTime,
            averageCompletionRate = completionRate
        )
    }
}
