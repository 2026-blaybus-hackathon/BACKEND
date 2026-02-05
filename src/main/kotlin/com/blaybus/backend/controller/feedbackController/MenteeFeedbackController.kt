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

    @Operation(summary = "특정 날짜 플래너의 종합 피드백 조회", description = "멘티는 멘토의 특정 날짜에 대한 종합 피드백을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "종합 피드백 조회 성공")
    @GetMapping("/daily-planner/total-feedback")
    fun getTotalFeedbackOfDailyPlanner(
        @AuthenticationPrincipal userId: Long,
        @Parameter(
            name = "date",
            description = "조회할 날짜 (YYYY-MM-DD 형식)",
            example = "2026-02-03",
            required = true,
        )
        @RequestParam date: LocalDate,
    ): ResponseEntity<FeedbackDto.GetTotalFeedbackResponse> =
        ResponseEntity
            .status(HttpStatus.OK)
            .body(
                feedbackService.findTotalFeedbackOfDailyPlanner(userId, date),
            )

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

    /**
     * TODO: 특정 날짜에 대한 피드백 목록 조회
     */
    @Operation(
        summary = "피드백 목록 조회",
        description = "멘티는 자신의 피드백 목록을 날짜별로 조회합니다.",
    )
    @ApiResponse(responseCode = "200", description = "멘티별 피드백 목록 조회 성공")
    @GetMapping("/feedbacks")
    fun getFeedbacks(
        @AuthenticationPrincipal userId: Long,
        @Parameter(
            name = "date",
            description = "조회할 날짜 (YYYY-MM-DD 형식)",
            example = "2026-02-03",
            required = true,
        )
        @RequestParam date: LocalDate,
    ): ResponseEntity<List<FeedbackDto.GetFeedbackOfTaskResponse>> =
        ResponseEntity
            .status(HttpStatus.OK)
            .body(
                feedbackService.findMyFeedbacks(userId, date),
            )
}
