package com.blaybus.backend.controller.taskController

import com.blaybus.backend.dto.MenteeTaskFeedbackResponse
import com.blaybus.backend.dto.MentorTaskAssignRequest
import com.blaybus.backend.dto.MentorTaskUpdateRequest
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
import org.springframework.web.bind.annotation.PatchMapping
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
        summary = "특정 멘티의 과제 및 피드백 목록 조회",
        description = "멘토가 특정 멘티의 과제 수행 내역과 피드백을 조회합니다.",
    )
    @GetMapping("/mentee/{menteeId}")
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

    @Operation(
        summary = "멘티에게 과제 할당 (자료실 연동)",
        description = "멘토가 멘티에게 과제를 할당합니다. \n" +
                "- `materialId`를 보내면 자료실 내용을 복사해서 할당합니다. \n" +
                "- `materialId` 없이 직접 입력하면 일반 과제로 할당됩니다."
    )
    @PostMapping(value = ["/assignment"], consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun assignTask(
        @AuthenticationPrincipal userId: Long,
        @Parameter(description = "과제 할당 요청 정보 (JSON)")
        @Valid @RequestPart("request") request: MentorTaskAssignRequest,

        @Parameter(description = "직접 파일 업로드 (materialId가 없을 때 사용)")
        @RequestPart("file", required = false) files: List<MultipartFile>?,
    ): ResponseEntity<TaskResponse> {
        val response = taskService.assignTask(userId, request, files)
        return ResponseEntity.ok(response)
    }

    @Operation(
        summary = "과제 수정 (멘토용)",
        description = "멘토가 할당한 과제의 내용, 날짜, 첨부파일을 수정합니다. (보내지 않은 값은 유지)",
    )
    @PatchMapping(
        value = ["/assignment/{taskId}"],
        consumes = [MediaType.MULTIPART_FORM_DATA_VALUE],
    )
    fun updateAssignedTask(
        @AuthenticationPrincipal userId: Long,
        @PathVariable taskId: Long,
        @Parameter(description = "수정할 과제 정보 (JSON)", content = [Content(mediaType = MediaType.APPLICATION_JSON_VALUE)])
        @Valid
        @RequestPart("request") request: MentorTaskUpdateRequest,
        @Parameter(description = "수정할 학습 자료 PDF (선택 사항 - 보내면 교체됨)")
        @RequestPart("file", required = false) files: List<MultipartFile>?,
    ): ResponseEntity<TaskResponse> {
        val response = taskService.updateAssignedTask(userId, taskId, request, files)
        return ResponseEntity.ok(response)
    }
}
