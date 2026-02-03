package com.blaybus.backend.controller

import com.blaybus.backend.dto.*
import com.blaybus.backend.service.TaskService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@Tag(name = "Task API", description = "할 일(Task) 관리, 과제 할당 및 인증")
@RequestMapping("/api/v1/tasks") // URL 변경: 자원 이름인 tasks 사용
@RestController
class TaskController(
    val taskService: TaskService,
) {

    // ================== 멘티 기능 (Task CRUD) ==================

    @Operation(summary = "Task 생성 (멘티)", description = "멘티가 자신의 플래너에 새로운 할 일을 등록합니다.")
    @PostMapping
    fun createTask(
        @AuthenticationPrincipal userId: Long,
        @Valid @RequestBody request: MenteeTaskCreateRequest
    ): ResponseEntity<TaskResponse> {
        return ResponseEntity.ok().build()
    }

    @Operation(summary = "Task 수정", description = "제목, 내용, 공부 시간 등을 수정합니다.")
    @PutMapping("/{taskId}")
    fun updateTask(
        @AuthenticationPrincipal userId: Long,
        @PathVariable taskId: Long,
        @Valid @RequestBody request: MenteeTaskUpdateRequest
    ): ResponseEntity<TaskResponse> {
        return ResponseEntity.ok().build()
    }

    @Operation(summary = "Task 삭제", description = "등록된 할 일을 삭제합니다.")
    @DeleteMapping("/{taskId}")
    fun deleteTask(
        @AuthenticationPrincipal userId: Long,
        @PathVariable taskId: Long
    ): ResponseEntity<Unit> {
        return ResponseEntity.ok().build()
    }

    @Operation(
        summary = "Task 공부 인증 사진 업로드",
        description = "멘티가 완료한 Task에 대해 인증 사진을 업로드합니다."
    )
    @PostMapping(value = ["/{taskId}/images"], consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun uploadVerificationImage(
        @AuthenticationPrincipal userId: Long,
        @Parameter(description = "인증할 Task의 ID") @PathVariable taskId: Long,
        @Parameter(description = "업로드할 이미지 파일") @RequestPart("image") image: MultipartFile
    ): ResponseEntity<FileUploadResponse> {
        return ResponseEntity.ok().build()
    }

    @Operation(
        summary = "코멘트 또는 질문",
        description = "멘티는 할 일에 멘토에게 코멘트 또는 질문을 남길 수 있다."
    )
    @PatchMapping("/{taskId}/comment")
    fun commentOnTask(
        @AuthenticationPrincipal userId: Long,
        @PathVariable taskId: Long,
        @RequestBody request: CommentOnTaskRequest
    ): ResponseEntity<Void> {
        taskService.updateComment(userId, taskId, request)

        return ResponseEntity.ok().build()
    }

    // ================== 멘토 기능 (과제 할당 및 조회) ==================

    @Operation(
        summary = "멘티에게 과제 할당 (PDF 포함)",
        description = "멘토가 특정 멘티의 플래너에 과제(Task)를 생성합니다. (경로 구분: /assignment)"
    )
    @PostMapping(value = ["/assignment"], consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun assignTask(
        @AuthenticationPrincipal userId: Long,
        @Parameter(description = "과제 정보 (JSON)") @Valid @RequestPart("request") request: MentorTaskAssignRequest,
        @Parameter(description = "학습 자료 PDF (선택 사항)") @RequestPart(
            "file",
            required = false
        ) file: MultipartFile?
    ): ResponseEntity<TaskResponse> {
        return ResponseEntity.ok().build()
    }

    @Operation(
        summary = "특정 멘티의 과제 및 피드백 목록 조회",
        description = "멘토가 특정 멘티의 과제 수행 내역과 피드백을 조회합니다."
    )
    @GetMapping("/mentee/{menteeId}") // URL 변경: /api/v1/tasks/mentee/{menteeId}
    fun getMenteeTasksWithFeedback(
        @AuthenticationPrincipal userId: Long,
        @PathVariable menteeId: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
    ): ResponseEntity<MenteeTaskFeedbackResponse> = ResponseEntity.ok().build()
}