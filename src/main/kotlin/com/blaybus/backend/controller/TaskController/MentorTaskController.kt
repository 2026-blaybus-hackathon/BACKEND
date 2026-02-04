package com.blaybus.backend.controller.TaskController

import com.blaybus.backend.dto.MenteeTaskFeedbackResponse
import com.blaybus.backend.dto.MentorTaskAssignRequest
import com.blaybus.backend.dto.TaskResponse
import com.blaybus.backend.service.TaskService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.data.domain.PageRequest
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@Tag(name = "mentor-task-controller", description = "멘토의 할 일(Task) 관리, 과제 할당 및 확인")
@RequestMapping("/api/v1/tasks/mentor")
@RestController
class MentorTaskController(
    private val taskService: TaskService,
) {
    @Operation(
        summary = "멘티에게 과제 할당 (PDF 포함)",
        description = "멘토가 특정 멘티의 플래너에 과제(Task)를 생성합니다.",
    )
    @PostMapping(value = ["/assignment"], consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun assignTask(
        @AuthenticationPrincipal userId: Long,
        @Parameter(
            description = "과제 정보 (JSON)",
            content = [Content(mediaType = MediaType.APPLICATION_JSON_VALUE)]
        )
        @Valid @RequestPart("request") request: MentorTaskAssignRequest,
        @Parameter(description = "학습 자료 PDF (선택 사항)") @RequestPart(
            "file",
            required = false,
        ) file: MultipartFile?,
    ): ResponseEntity<TaskResponse> {
        val response = taskService.assignTask(userId, request, file)
        return ResponseEntity.ok(response)
    }

    @Operation(
        summary = "특정 멘티의 과제 및 피드백 목록 조회",
        description = "멘토가 특정 멘티의 과제 수행 내역과 피드백을 조회합니다.",
    )
    @GetMapping("/mentee/{menteeId}") // URL 변경: /api/v1/tasks/mentee/{menteeId}
    fun getMenteeTasksWithFeedback(
        @AuthenticationPrincipal userId: Long,
        @PathVariable menteeId: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
    ): ResponseEntity<MenteeTaskFeedbackResponse> {
        val pageable = PageRequest.of(page, size)
        val response = taskService.getMenteeTasksWithFeedback(userId, menteeId, pageable)
        return ResponseEntity.ok(response)
    }
}