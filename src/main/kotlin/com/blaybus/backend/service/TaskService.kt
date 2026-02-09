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
import com.blaybus.backend.dto.TaskImageResponse
import com.blaybus.backend.dto.TaskResponse
import com.blaybus.backend.dto.TaskSummaryResponse
import com.blaybus.backend.dto.TaskWithFeedbackResponse
import com.blaybus.backend.entity.Assignment
import com.blaybus.backend.entity.LearningMaterial
import com.blaybus.backend.entity.Role
import com.blaybus.backend.entity.StudyImage
import com.blaybus.backend.entity.Subject
import com.blaybus.backend.entity.Task
import com.blaybus.backend.entity.TaskType
import com.blaybus.backend.entity.User
import com.blaybus.backend.exception.CustomException
import com.blaybus.backend.exception.ErrorCode
import com.blaybus.backend.repository.AssignmentRepository
import com.blaybus.backend.repository.LearningMaterialRepository
import com.blaybus.backend.repository.ObjectStorageRepository
import com.blaybus.backend.repository.StudyImageRepository
import com.blaybus.backend.repository.TaskRepository
import com.blaybus.backend.repository.UserRepository
import com.blaybus.backend.repository.getByTaskId
import com.blaybus.backend.repository.getByUserId
import com.blaybus.backend.repository.getTaskAndDailyPlannerById
import com.blaybus.backend.service.auth.AuthService
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
    private val learningMaterialRepository: LearningMaterialRepository,
    private val authService: AuthService,
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

        // 1. Task 정보 준비 (자료실 선택 시 덮어쓰기)
        var title = request.title
        var content = request.content
        var subject = request.subject
        var taskType = request.taskType

        var materialFileKey: String? = null
        var materialFileName: String? = null

        // 자료실 선택 시 정보 조회
        if (request.materialId != null) {
            val material = learningMaterialRepository.findById(request.materialId)
                .orElseThrow { CustomException(ErrorCode.RESOURCE_NOT_FOUND) }

            if (material.mentor.id != mentorId) throw CustomException(ErrorCode.NOT_YOUR_MATERIAL)

            title = material.title
            content = material.content ?: request.content
            subject = material.subject
            taskType = material.taskType
            materialFileKey = material.fileKey
            materialFileName = material.originalFileName
        }

        // 2. 통합 메서드 호출
        // 파일이 있고 + 자료실 ID가 없으면 -> "직접 업로드" 상황이므로 -> 자동 아카이빙(true) 요청
        val shouldArchive = (!files.isNullOrEmpty() && request.materialId == null)

        return createAndSaveTask(
            writer = mentor,
            targetMentee = mentee,
            date = request.date,
            taskType = taskType,
            title = title,
            content = content,
            subject = subject,
            files = files,
            // 추가 파라미터 전달
            materialFileKey = materialFileKey,
            materialFileName = materialFileName,
            isAutoArchive = shouldArchive
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

        if (task.writer.id != userId) {
            throw CustomException(ErrorCode.NOT_YOUR_TASK)
        }

        task.title = request.title ?: task.title
        task.content = request.content ?: task.content
        task.subject = request.subject ?: task.subject

        if (!files.isNullOrEmpty()) {
            val uploadPath = "tasks/${task.id}/assignments/"
            manageAssignmentFiles(task, files, uploadPath)
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
        subject: Subject?,
        lastId: Long?,
        size: Int,
    ): SliceResponse<TaskAndAssignmentResponse> {
        val user = userRepository.getByUserId(userId)
        val dailyPlanner = dailyPlannerService.getDailyPlannerOrNullByUserAndDate(user, date)
            ?: return SliceResponse(emptyList(), false)

        val pageable = Pageable.ofSize(size)

        // [변경 로직] subject가 있으면 과목별 조회, 없으면 전체 조회
        val tasks = if (subject != null) {
            taskRepository.sliceByDailyPlannerIdAndSubject(dailyPlanner.id, subject, lastId, pageable)
        } else {
            taskRepository.sliceByDailyPlannerId(dailyPlanner.id, lastId, pageable)
        }

        return SliceResponse(
            tasks.content.map { task ->
                val assignmentList = assignmentRepository.findAllByTask(task).map {
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
    fun getByTaskId(
        userId: Long,
        taskId: Long,
    ): TaskWithFeedbackResponse {
        val user = userRepository.getByUserId(userId)
        val task = taskRepository.getTaskAndDailyPlannerById(taskId)

        when (user.role) {
            Role.MENTOR -> {
                user.validateMentee(task.dailyPlanner.user)
            }

            Role.MENTEE -> {
                user.validateSameUser(task.dailyPlanner.user)
            }
        }

        return TaskWithFeedbackResponse(
            menteeId = task.dailyPlanner.user.id,
            tasks =
                PagedResponse(
                    content = listOf(toTaskDetail(task)),
                    page = 0,
                    size = 1,
                    totalPages = 1,
                    totalElements = 1,
                ),
            totalFeedback = task.dailyPlanner.totalFeedback,
        )
    }

    fun getTasksWithFeedback(
        userId: Long,
        menteeId: Long?,
        pageable: Pageable,
    ): List<TaskWithFeedbackResponse> {
        val user = userRepository.getByUserId(userId)
        val tagetMentee = authService.validateMentorAccessAndGetTargetUser(user, menteeId)

        if (user.role == Role.MENTOR) {
            val yesterday = LocalDate.now().minusDays(1)
            val tasks = taskRepository.findByDailyPlannerUserAndDailyPlannerDate(tagetMentee, yesterday)
            if (tasks.isEmpty()) return emptyList()

            val dailyPlanner = tasks.first().dailyPlanner
            val taskDetails = tasks.map { toTaskDetail(it) }

            return listOf(
                TaskWithFeedbackResponse(
                    menteeId = tagetMentee.id,
                    tasks =
                        PagedResponse(
                            content = taskDetails,
                            page = 0,
                            size = taskDetails.size,
                            totalPages = 1,
                            totalElements = taskDetails.size.toLong(),
                        ),
                    totalFeedback = dailyPlanner.totalFeedback,
                ),
            )
        }

        val tasksPage = taskRepository.findByDailyPlannerUser(tagetMentee, pageable)

        // dailyPlanner별로 그룹화하고 날짜 내림차순으로 정렬하여 최근 2일치만 선택
        val tasksByDailyPlanner =
            tasksPage.content
                .groupBy { it.dailyPlanner }
                .toList()
                .sortedByDescending { (dailyPlanner, _) -> dailyPlanner.date }
                .take(2)

        return tasksByDailyPlanner.map { (dailyPlanner, tasks) ->
            val taskDetails = tasks.map { toTaskDetail(it) }

            TaskWithFeedbackResponse(
                menteeId = tagetMentee.id,
                tasks =
                    PagedResponse(
                        content = taskDetails,
                        page = tasksPage.number,
                        size = taskDetails.size,
                        totalPages = 1,
                        totalElements = taskDetails.size.toLong(),
                    ),
                totalFeedback = dailyPlanner.totalFeedback,
            )
        }
    }

    fun getTaskSummaries(
        userId: Long,
        menteeId: Long,
        pageable: Pageable,
    ): PagedResponse<TaskSummaryResponse> {
        val mentor = userRepository.getByUserId(userId)
        val mentee = userRepository.getByUserId(menteeId)
        mentor.validateMentee(mentee)

        val tasksPage = taskRepository.findTasksWithFeedbackByMenteeAndMentor(mentee.id, mentor.id, pageable)

        return PagedResponse(
            content =
                tasksPage.content.map { task ->
                    TaskSummaryResponse(
                        taskId = task.id,
                        subject = task.subject.name,
                        title = task.title,
                        date = task.createdDateTime.toLocalDate(),
                    )
                },
            page = tasksPage.number,
            size = tasksPage.size,
            totalPages = tasksPage.totalPages,
            totalElements = tasksPage.totalElements,
        )
    }

    private fun toTaskDetail(task: Task): TaskDetail =
        TaskDetail(
            taskId = task.id,
            subject = task.subject.name,
            title = task.title,
            time = task.studyDurationInMinutes,
            date = task.createdDateTime.toLocalDate(),
            status = task.isCompleted,
            menteeComment = task.comment,
            feedbackStatus = task.feedbackStatus().name,
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
                        content = fb.detail,
                    )
                } ?: FeedbackDetail(0L, null),
        )

    private fun createAndSaveTask(
        writer: User,
        targetMentee: User,
        date: LocalDate,
        taskType: TaskType,
        title: String,
        content: String?,
        subject: Subject,
        files: List<MultipartFile>?,
        // [New] 자료실 연동을 위한 파라미터들
        materialFileKey: String? = null,
        materialFileName: String? = null,
        isAutoArchive: Boolean = false
    ): TaskResponse {
        val planner = dailyPlannerService.getOrCreateDailyPlannerByDate(targetMentee, date)

        val task = taskRepository.save(
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

        // 파일 처리 로직
        if (materialFileKey != null && materialFileName != null) {
            // [Case A] 기존 자료실 파일 재사용 (S3 업로드 X, DB만 연결)
            assignmentRepository.save(
                Assignment(
                    task = task,
                    fileKey = materialFileKey,
                    originalFileName = materialFileName
                )
            )
        } else if (!files.isNullOrEmpty()) {
            // [Case B] 신규 파일 업로드 (기존 메서드 재사용)
            // 멘토가 올리는 파일이면 경로를 'materials/'로 하여 관리 편의성 증대 (선택사항)
            val uploadPath = if (isAutoArchive) "materials/${writer.id}/" else "tasks/${task.id}/assignments/"

            val newAssignments = manageAssignmentFiles(task, files, uploadPath)

            // [Auto Archive] 멘토가 직접 올린 파일이면 -> 자료실(LearningMaterial)에도 자동 등록
            if (isAutoArchive) {
                newAssignments.forEach { assignment ->
                    learningMaterialRepository.save(
                        LearningMaterial(
                            mentor = writer,
                            title = title,
                            taskType = taskType,
                            subject = subject,
                            content = content,
                            fileKey = assignment.fileKey,
                            originalFileName = assignment.originalFileName
                        )
                    )
                }
            }
        }

        return TaskResponse.from(task)
    }

    private fun manageAssignmentFiles(
        task: Task,
        files: List<MultipartFile>,
        pathPrefix: String
    ): List<Assignment> {
        val existingAssignments = assignmentRepository.findAllByTask(task)
        if (existingAssignments.isNotEmpty()) {
            assignmentRepository.deleteAll(existingAssignments)
            assignmentRepository.flush()
        }

        return files.map { file ->
            val uploadedKey = objectStorageRepository.upload(pathPrefix, file)

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
            averageCompletionRate = completionRate,
        )
    }
}
