package com.blaybus.backend.controller

import com.blaybus.backend.dto.CommentOnTaskRequest
import com.blaybus.backend.dto.FileUploadResponse
import com.blaybus.backend.dto.MenteeTaskCreateRequest
import com.blaybus.backend.dto.MenteeTaskFeedbackResponse
import com.blaybus.backend.dto.MenteeTaskUpdateRequest
import com.blaybus.backend.dto.MentorTaskAssignRequest
import com.blaybus.backend.dto.TaskResponse
import com.blaybus.backend.service.TaskService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.data.domain.PageRequest
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@Tag(name = "task-controller", description = "할 일(Task) 관리, 과제 할당 및 인증")
@RequestMapping("/api/v1/tasks")
@RestController
class TaskController(
    private val taskService: TaskService,
) {
    // ================== 멘티 기능 (Task CRUD) ==================

    // TODO: 멘티가 할 일을 생성할 때도 파일 업로드를 선택적으로 할 수 있습니다. (피그마 유저 플로우 참고해주시기 바랍니다.)
    @Operation(summary = "Task 생성 (멘티)", description = "멘티가 자신의 플래너에 새로운 할 일을 등록합니다.")
    @PostMapping
    fun createTask(
        @AuthenticationPrincipal userId: Long,
        @Valid @RequestBody request: MenteeTaskCreateRequest,
    ): ResponseEntity<TaskResponse> {
        val response = taskService.createTask(userId, request)
        return ResponseEntity.ok(response)
    }

    // TODO: 이 경우에 공부 시간만 또는 완료 여부만 수정할 때도 모든 데이터를 넣어서 request를 해야합니다.
    //  1. 일부 컬럼에 대한 변경인데 PUT Method를 사용한다는 것
    //  2. null을 보내는 것과 값을 보내지 않는 것을 구분하지 못한다는 점
    //  위 내용을 인지하고 계시고 작업한 것이라면 수정하지 않으셔도 됩니다.
    @Operation(summary = "Task 수정", description = "제목, 내용, 공부 시간 등을 수정합니다.")
    @PutMapping("/{taskId}")
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

    // ================== 멘토 기능 (과제 할당 및 조회) ==================

    // TODO: 멘토가 과제를 부여할 때는 파일 업로드가 필수입니다. (피그마 유저 플로우 참고해주시기 바랍니다.)
    @Operation(
        summary = "멘티에게 과제 할당 (PDF 포함)",
        description = "멘토가 특정 멘티의 플래너에 과제(Task)를 생성합니다.",
    )
    @PostMapping(value = ["/assignment"], consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun assignTask(
        @AuthenticationPrincipal userId: Long,
        @Parameter(description = "과제 정보 (JSON)") @Valid @RequestPart("request") request: MentorTaskAssignRequest,
        @Parameter(description = "학습 자료 PDF (선택 사항)") @RequestPart(
            "file",
            required = false,
        ) file: MultipartFile?,
    ): ResponseEntity<TaskResponse> {
        val response = taskService.assignTask(userId, request, file)
        return ResponseEntity.ok(response)
    }

    // TODO:
    //  이 부분에서 페이징이 필요한 것인지 궁금합니다. 지금 형태라면 멘티가 작성한 처음 과제부터 페이징해서 내려주는데 그럼 1년 뒤에 조회했을 떄도 1년 전에 작성한 내용부터 조회가 되는 것같습니다.
    //  날짜별로 할 일의 개수가 일정해 특정 날짜(특정 페이지)의 할 일을 요청하는 것이 가능하다면 괜찮지만 그러기에는 어려움이 있어 보입니다.
    //  -> 날짜를 파라미터로 받아서 해당 날짜에 대한 내용만 주는 것이 어떤지 여쭤봅니다.
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
