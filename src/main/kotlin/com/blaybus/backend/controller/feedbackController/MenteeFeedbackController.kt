package com.blaybus.backend.controller.feedbackController

import com.blaybus.backend.dto.FeedbackDto
import com.blaybus.backend.service.FeedbackService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

@RestController
@RequestMapping("/api/v1/feedback/mentee")
class MenteeFeedbackController(
    private val feedbackService: FeedbackService,
) {
    private val logger = KotlinLogging.logger {}

    @Deprecated("/api/v1/tasks 쪽으로 통합")
    @Operation(summary = "할 일의 피드백 요약, 상세 조회", description = "멘티는 멘토의 피드백 요약, 상세 내용을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "피드백 상세 조회 성공")
    @GetMapping("/tasks/{taskId}")
    fun getFeedbackOfTask(
        @AuthenticationPrincipal userId: Long,
        @Parameter(
            name = "taskId",
            description = "피드백을 조회할 할 일 ID",
            required = true,
            example = "1",
        )
        @PathVariable taskId: Long,
    ): ResponseEntity<FeedbackDto.GetFeedbackOfTaskResponse> =
        ResponseEntity
            .status(HttpStatus.OK)
            .body(
                feedbackService.findFeedbackOfTask(userId, taskId),
            )

    @Operation(
        summary = "안 읽은 피드백 개수 조회",
        description = "멘티는 자신이 아직 읽지 않은 피드백의 개수를 조회합니다.",
    )
    @GetMapping("/unread-count")
    fun getUnreadFeedbackCount(
        @AuthenticationPrincipal userId: Long,
    ): ResponseEntity<FeedbackDto.GetUnreadFeedbackCountResponse> =
        ResponseEntity.ok(
            feedbackService.unreadFeedbackCount(userId),
        )

    @Operation(
        summary = "안 읽은 피드백 리스트 조회",
        description = "멘티는 자신이 아직 읽지 않은 피드백 리스트를 조회합니다.",
    )
    @GetMapping("/unread-feedbacks")
    fun getUnreadFeedbacks(
        @AuthenticationPrincipal userId: Long,
    ): ResponseEntity<List<FeedbackDto.GetUnreadFeedbackResponse>> =
        ResponseEntity.ok(
            feedbackService.getUnreadFeedbacks(userId),
        )

    @Operation(
        summary = "피드백 상세 조회",
        description = "멘티는 피드백 ID로 특정 피드백을 조회합니다.",
    )
    @GetMapping("/{feedbackId}")
    fun getFeedbackById(
        @AuthenticationPrincipal userId: Long,
        @Parameter(
            name = "feedbackId",
            description = "조회할 피드백 ID",
            example = "1",
            required = true,
        )
        @PathVariable feedbackId: Long,
    ): ResponseEntity<FeedbackDto.GetFeedbackByIdResponse> = ResponseEntity.ok(feedbackService.getByFeedbackId(userId, feedbackId))
}
