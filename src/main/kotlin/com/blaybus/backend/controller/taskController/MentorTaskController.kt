package com.blaybus.backend.controller.taskController

import com.blaybus.backend.dto.DayOfTaskWithFeedbackResponse
import com.blaybus.backend.dto.MentorTaskAssignRequest
import com.blaybus.backend.dto.MentorTaskUpdateRequest
import com.blaybus.backend.dto.PagedResponse
import com.blaybus.backend.dto.TaskInfoResponse
import com.blaybus.backend.dto.TaskSummaryResponse
import com.blaybus.backend.dto.TaskResponse
import com.blaybus.backend.dto.TaskWithFeedbackResponse
import com.blaybus.backend.service.TaskService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
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
        summary = "멘티에게 과제 할당 (PDF 포함)",
        description = "멘토가 특정 멘티의 플래너에 과제(Task)를 생성합니다.",
    )
    @PostMapping(value = ["/assignment"], consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun assignTask(
        @AuthenticationPrincipal userId: Long,
        @Parameter(
            description = "과제 할당 요청 정보 (JSON)",
            content = [Content(mediaType = MediaType.APPLICATION_JSON_VALUE)]
        )
        @Valid
        @RequestPart("request") request: MentorTaskAssignRequest,
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
        @Parameter(
            description = "수정할 과제 정보 (JSON)",
            content = [Content(mediaType = MediaType.APPLICATION_JSON_VALUE)]
        )
        @Valid
        @RequestPart("request") request: MentorTaskUpdateRequest,
        @Parameter(description = "수정할 학습 자료 PDF (선택 사항 - 보내면 교체됨)")
        @RequestPart("file", required = false) files: List<MultipartFile>?,
    ): ResponseEntity<TaskResponse> {
        val response = taskService.updateAssignedTask(userId, taskId, request, files)
        return ResponseEntity.ok(response)
    }

    @Operation(
        summary = "멘티별 task 조회",
        description = "멘티별로 어제의 task 목록을 조회한다.",
    )
    @GetMapping("/{menteeId}")
    fun getYesterDayTasks(
        @AuthenticationPrincipal userId: Long,
        @PathVariable menteeId: Long
    ): ResponseEntity<DayOfTaskWithFeedbackResponse> {
        val response =
            taskService.getYesterdaysTaskDetailByMenteeId(userId, menteeId)
        return ResponseEntity.ok(response)
    }

    @Operation(
        summary = "최근 피드백을 작성한 task 조회",
        description = "task 목록 최근 피드백을 작성한 순서대로 응답한다.",
    )
    @GetMapping("/recent-feedbacks/{menteeId}")
    fun getTasksOrderByFeedbackRecently(
        @AuthenticationPrincipal userId: Long,
        @PathVariable menteeId: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
    ): ResponseEntity<PagedResponse<TaskSummaryResponse>> {
        val pageable = PageRequest.of(page, size)
        val response =
            taskService.getAssignmentSummaryOfFeedbacksRecently(userId, menteeId, pageable)
        return ResponseEntity.ok(response)
    }

    @Operation(
        summary = "제출된 과제 목록 조회",
        description = "제출된 모든 멘티의 과제 목록을 응답한다.",
    )
    @GetMapping("/assignments")
    fun getAssignments(
        @AuthenticationPrincipal userId: Long,
        @Parameter(
            description = "조회 타입 (REMIND: 제출되었으나 피드백 미완료, HISTORY: 제출 및 피드백 완료)",
            schema = Schema(allowableValues = ["REMIND", "HISTORY"])
        )
        @RequestParam type: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
    ): ResponseEntity<PagedResponse<TaskInfoResponse>> {
        val pageable = PageRequest.of(page, size)
        val response = taskService.getAllAssignmentsInfo(userId, type, pageable)
        return ResponseEntity.ok(response)
    }

    @Operation(
        summary = "멘티별 제출된 과제 목록 조회",
        description = "특정 멘티의 제출된 과제 목록을 응답한다. type이 REMIND라면 과제는 제출되었으나 피드백 완료하지 않은 Task, type이 HISTORY라면 과제 제출과 멘토의 피드백 작성이 완료인 Task를 응답한다.",
    )
    @GetMapping("/assignments/{menteeId}")
    fun getAssignmentsByMenteeId(
        @AuthenticationPrincipal userId: Long,
        @PathVariable menteeId: Long,
        @Parameter(
            description = "조회 타입 (REMIND: 제출되었으나 피드백 미완료, HISTORY: 제출 및 피드백 완료)",
            schema = Schema(allowableValues = ["REMIND", "HISTORY"])
        )
        @RequestParam type: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
    ): ResponseEntity<PagedResponse<TaskInfoResponse>> {
        val pageable = PageRequest.of(page, size)
        val response = taskService.getAllAssignmentsInfoByMenteeId(userId, menteeId, type, pageable)
        return ResponseEntity.ok(response)
    }
}
