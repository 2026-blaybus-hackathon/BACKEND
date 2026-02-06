package com.blaybus.backend.service

import com.blaybus.backend.dto.AchievementDetails
import com.blaybus.backend.dto.AchievementRateResponse
import com.blaybus.backend.dto.CommentOnTaskRequest
import com.blaybus.backend.dto.FeedbackDetail
import com.blaybus.backend.dto.FileUploadResponse
import com.blaybus.backend.dto.MenteeTaskCreateRequest
import com.blaybus.backend.dto.MenteeTaskFeedbackResponse
import com.blaybus.backend.dto.MenteeTaskUpdateRequest
import com.blaybus.backend.dto.MentorTaskAssignRequest
import com.blaybus.backend.dto.PagedResponse
import com.blaybus.backend.dto.TaskDetail
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
import com.blaybus.backend.repository.getTaskWithDailyPlannerById
import com.blaybus.backend.util.getMondayOfWeek
import com.blaybus.backend.util.getWeekRange
import mu.KotlinLogging
import org.redisson.api.RedissonClient
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.time.Duration
import java.time.LocalDate

@Service
@Transactional(readOnly = true)
class TaskService(
    private val taskRepository: TaskRepository,
    private val userRepository: UserRepository,
    private val assignmentRepository: AssignmentRepository,
    private val studyImageRepository: StudyImageRepository,
    private val objectStorageRepository: ObjectStorageRepository,
    private val dailyPlannerService: DailyPlannerService,
    private val redissonClient: RedissonClient,
) {
    private val logger = KotlinLogging.logger {}

    private val achievementRateCacheKey = "achievement-rate"

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
        deleteWeeklyAchievementRateCache(userId, request.date)
        return TaskResponse(
            id = task.id,
            content = task.title,
            subject = task.subject,
            priority = null,
            studyTime = task.studyDurationInMinutes ?: 0,
        )
    }

    @Transactional
    fun updateTask(
        userId: Long,
        taskId: Long,
        request: MenteeTaskUpdateRequest,
    ): TaskResponse {
        val task = taskRepository.getTaskWithDailyPlannerById(taskId)

        if (task.writer.id != userId) throw CustomException(ErrorCode.NOT_YOUR_TASK)

        task.title = request.title
        task.content = request.content
        task.studyDurationInMinutes = request.studyTime // studyTime으로 매핑
        task.isCompleted = request.isCompleted ?: false
        deleteWeeklyAchievementRateCache(userId, task.dailyPlanner.date)
        return TaskResponse(
            id = task.id,
            content = task.title,
            subject = task.subject,
            priority = null,
            studyTime = task.studyDurationInMinutes ?: 0,
        )
    }

    @Transactional
    fun deleteTask(
        userId: Long,
        taskId: Long,
    ) {
        val task = taskRepository.getTaskWithDailyPlannerById(taskId)
        if (task.writer.id != userId) throw CustomException(ErrorCode.NOT_YOUR_TASK)
        taskRepository.delete(task)
        deleteWeeklyAchievementRateCache(userId, task.dailyPlanner.date)
    }

    @Transactional
    fun uploadVerificationImage(
        userId: Long,
        taskId: Long,
        image: MultipartFile,
    ): FileUploadResponse {
        val task = taskRepository.getByTaskId(taskId)
        if (task.writer.id != userId) throw CustomException(ErrorCode.NOT_YOUR_TASK)

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
    fun getAchievementRate(
        userId: Long,
        date: LocalDate,
    ): AchievementRateResponse {
        userRepository.getByUserId(userId)
        val (startOfWeek, endOfWeek) = getWeekRange(date)
        val bucket =
            redissonClient.getBucket<AchievementRateResponse>("$achievementRateCacheKey::$userId::$startOfWeek::")
        val response = bucket.get()
        if (response != null) {
            logger.info("캐시에서 주간 달성률 정보 조회됨")
            return response
        }
        val dailyPlannerList = dailyPlannerService.getDailyPlannerByPeriod(userId, startOfWeek, endOfWeek)
        val todayTask = dailyPlannerList.filter { it.date == date }.flatMap { it.tasks }
        val todayCompleteTaskSize = todayTask.filter { it.isCompleted }.size
        val weeklyTaskList = dailyPlannerList.flatMap { it.tasks }
        val weeklyStudyTimeMinutes =
            weeklyTaskList.mapNotNull { it.studyDurationInMinutes }.sum()
        val weeklyCompleteTaskSize = weeklyTaskList.filter { it.isCompleted }.size
        val achievementRateResponse =
            AchievementRateResponse(
                weekly = AchievementDetails(weeklyCompleteTaskSize, weeklyTaskList.size),
                today = AchievementDetails(todayCompleteTaskSize, todayTask.size),
                weeklyStudyTimeMinutes = weeklyStudyTimeMinutes,
            )
        bucket.set(achievementRateResponse, Duration.ofHours(24))
        logger.info("$userId : 주간 달성률 정보 캐시에 저장됨")
        return achievementRateResponse
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
        deleteWeeklyAchievementRateCache(request.menteeId, request.date)
        return TaskResponse(
            id = task.id,
            content = task.title,
            subject = task.subject,
            priority = null,
            studyTime = task.studyDurationInMinutes ?: 0,
        )
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

    private fun deleteWeeklyAchievementRateCache(
        userId: Long,
        date: LocalDate,
    ) {
        val startOfWeek = getMondayOfWeek(date)
        val bucket =
            redissonClient.getBucket<AchievementRateResponse>("$achievementRateCacheKey::$userId::$startOfWeek::")
        if (bucket.isExists) {
            bucket.delete()
            logger.info("$userId : 주간 달성률 캐시 삭제됨")
        }
    }
}
