package com.blaybus.backend.controller.taskController

import com.blaybus.backend.dto.CommentOnTaskRequest
import com.blaybus.backend.dto.DailyAchievementRate
import com.blaybus.backend.dto.FileUploadResponse
import com.blaybus.backend.dto.MenteeStudyTimeUpdateRequest
import com.blaybus.backend.dto.MenteeTaskCompletionUpdateRequest
import com.blaybus.backend.dto.MenteeTaskCreateRequest
import com.blaybus.backend.dto.MenteeTaskUpdateRequest
import com.blaybus.backend.dto.TaskResponse
import com.blaybus.backend.service.TaskService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDate

@Tag(name = "mentee-task-controller", description = "멘티의 할 일(Task) 관리, CRUD")
@RequestMapping("/api/v1/tasks/mentee")
@RestController
class MenteeTaskController(
    private val taskService: TaskService,
) {
    @Operation(summary = "Task 생성 (멘티)", description = "멘티가 자신의 플래너에 새로운 할 일을 등록합니다.")
    @PostMapping
    fun createTask(
        @AuthenticationPrincipal userId: Long,
        @Valid @RequestBody request: MenteeTaskCreateRequest,
        @Parameter(description = "학습 자료 PDF들 (선택 사항)") @RequestPart(
            "file",
            required = false,
        ) files: List<MultipartFile>?,
    ): ResponseEntity<TaskResponse> {
        val response = taskService.createTask(userId, request, files)
        return ResponseEntity.ok(response)
    }

    @Operation(summary = "Task 전체 수정", description = "제목, 내용, 공부 시간 등을 수정합니다.")
    @PatchMapping("/{taskId}")
    fun updateTask(
        @AuthenticationPrincipal userId: Long,
        @PathVariable taskId: Long,
        @Valid @RequestBody request: MenteeTaskUpdateRequest,
    ): ResponseEntity<TaskResponse> {
        val response = taskService.updateTask(userId, taskId, request)
        return ResponseEntity.ok(response)
    }

    @Operation(summary = "Task 삭제", description = "등록된 할 일을 삭제합니다.")
    @DeleteMapping("/{taskId}")
    fun deleteTask(
        @AuthenticationPrincipal userId: Long,
        @PathVariable taskId: Long,
    ): ResponseEntity<Unit> {
        taskService.deleteTask(userId, taskId)
        return ResponseEntity.noContent().build()
    }

    @Operation(
        summary = "Task 공부 인증 사진 업로드",
        description = "멘티가 완료한 Task에 대해 인증 사진을 업로드합니다.",
    )
    @PostMapping(value = ["/{taskId}/images"], consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun uploadVerificationImage(
        @AuthenticationPrincipal userId: Long,
        @Parameter(description = "인증할 Task의 ID") @PathVariable taskId: Long,
        @Parameter(description = "업로드할 이미지 파일") @RequestPart("image") image: MultipartFile,
    ): ResponseEntity<FileUploadResponse> {
        val response = taskService.uploadVerificationImage(userId, taskId, image)
        return ResponseEntity.ok(response)
    }

    @Operation(
        summary = "코멘트 또는 질문",
        description = "멘티는 할 일에 멘토에게 코멘트 또는 질문을 남길 수 있다.",
    )
    @PatchMapping("/{taskId}/comment")
    fun commentOnTask(
        @AuthenticationPrincipal userId: Long,
        @PathVariable taskId: Long,
        @RequestBody request: CommentOnTaskRequest,
    ): ResponseEntity<Void> {
        taskService.updateComment(userId, taskId, request)

        return ResponseEntity.ok().build()
    }

    @Operation(
        summary = "공부시간 수정",
        description = "멘티는 공부시간(분 단위)을 기록하고 수정할 수 있다.",
    )
    @PatchMapping("/{taskId}/studyTime")
    fun updateStudyTime(
        @AuthenticationPrincipal userId: Long,
        @Parameter(description = "Task ID") @PathVariable taskId: Long,
        @Valid @RequestBody request: MenteeStudyTimeUpdateRequest,
    ): ResponseEntity<TaskResponse> {
        val response = taskService.updateStudyTime(userId, taskId, request)
        return ResponseEntity.ok(response)
    }

    @Operation(
        summary = "완료여부 수정",
        description = "멘티는 자신이 생성한 Task의 완료 여부를 체크할 수 있다.",
    )
    @PatchMapping("/{taskId}/isCompleted")
    fun updateIsCompleted(
        @AuthenticationPrincipal userId: Long,
        @Parameter(description = "Task ID") @PathVariable taskId: Long,
        @Valid @RequestBody request: MenteeTaskCompletionUpdateRequest,
    ): ResponseEntity<TaskResponse> {
        val response = taskService.updateTaskCompletion(userId, taskId, request)
        return ResponseEntity.ok(response)
    }

    @Operation(
        summary = "주간 유저 달성 정보 조회",
        description = "멘티는 자신의 주간 달성 정보를 조회할 수 있습니다.(유저 주간 히트맵)",
    )
    @GetMapping("/weekly-achievement-rate")
    fun getWeeklyAchievement(
        @AuthenticationPrincipal userId: Long,
        @Parameter(
            name = "date",
            description = "조회할 날짜 (해당 날짜가 속한 주의 달성 정보가 조회됩니다)",
            required = true,
        )
        @RequestParam date: LocalDate,
    ): ResponseEntity<List<DailyAchievementRate>> = ResponseEntity.ok(taskService.getWeeklyAchievement(userId, date))
}
