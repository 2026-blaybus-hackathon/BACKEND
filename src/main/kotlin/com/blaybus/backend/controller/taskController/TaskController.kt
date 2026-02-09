package com.blaybus.backend.controller.taskController

import com.blaybus.backend.annotation.ApiErrorCodes
import com.blaybus.backend.dto.TaskWithFeedbackResponse
import com.blaybus.backend.exception.ErrorCode
import com.blaybus.backend.service.TaskService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.PageRequest
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Tag(name = "common-task-controller", description = "공통 권한 할 일(Task) 관련 API")
@RequestMapping("/api/v1/tasks")
@RestController
class TaskController(
    private val taskService: TaskService,
) {
    @Operation(
        summary = "특정 멘티의 과제 및 피드백 목록 조회",
        description = "특정 멘티의 과제 및 피드백 관련 정보를 조회합니다.",
    )
    @GetMapping("/mentees/{menteeId}")
    fun getMenteeTasksWithFeedback(
        @AuthenticationPrincipal userId: Long,
        @PathVariable menteeId: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
    ): ResponseEntity<List<TaskWithFeedbackResponse>> {
        val pageable = PageRequest.of(page, size)
        val response = taskService.getTasksWithFeedback(userId, menteeId, pageable)
        return ResponseEntity.ok(response)
    }

    @ApiErrorCodes(ErrorCode.NOT_YOUR_TASK)
    @Operation(
        summary = "할 일 ID로 상세 조회",
        description = "멘티는 할 일 ID로 해당 할 일의 상세 정보를 조회할 수 있습니다.",
    )
    @GetMapping("/{taskId}")
    fun getTaskById(
        @AuthenticationPrincipal userId: Long,
        @Parameter(
            name = "taskId",
            description = "조회할 할 일 ID",
            example = "1",
            required = true,
        )
        @PathVariable taskId: Long,
    ): ResponseEntity<TaskWithFeedbackResponse> {
        val task = taskService.getByTaskId(userId, taskId)
        return ResponseEntity.ok(task)
    }
}
