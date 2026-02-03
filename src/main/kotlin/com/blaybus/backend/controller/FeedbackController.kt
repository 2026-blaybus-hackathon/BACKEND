package com.blaybus.backend.controller

import com.blaybus.backend.annotation.ApiErrorCodes
import com.blaybus.backend.dto.FeedbackDto
import com.blaybus.backend.exception.ErrorCode
import com.blaybus.backend.service.FeedbackService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import jakarta.validation.Valid
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate
import java.time.LocalDateTime

@RestController
@RequestMapping("/api/v1")
class FeedbackController(
    private val feedbackService: FeedbackService,
) {
    private val logger = KotlinLogging.logger {}

    @ApiErrorCodes(
        ErrorCode.FORBIDDEN_FOR_CREATE_FEEDBACK,
        ErrorCode.NOT_MY_MENTEE,
    )
    @Operation(summary = "피드백 작성(요약, 상세)", description = "멘토는 멘티의 할 일에 피드백 요약과 상세 내용을 작성합니다.")
    @ApiResponse(responseCode = "201", description = "피드백 작성 성공")
    @PostMapping("/tasks/{taskId}/feedback")
    fun provideFeedback(
        @AuthenticationPrincipal userId: Long,
        @Valid @RequestBody request: FeedbackDto.CreateFeedbackRequest,
        @Parameter(
            name = "taskId",
            description = "피드백을 작성할 할 일 ID",
            required = true,
            example = "1"
        )
        @PathVariable taskId: Long
    ): ResponseEntity<FeedbackDto.CreateFeedbackResponse> {

        val createdFeedbackId =
            feedbackService.provideFeedbackForMenteesTask(userId, taskId, request)

        // 임시로 1L 응답
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(
                FeedbackDto.CreateFeedbackResponse(createdFeedbackId)
            )
    }

    @ApiErrorCodes(
        ErrorCode.FORBIDDEN_FOR_CREATE_FEEDBACK,
        ErrorCode.NOT_MY_MENTEE,
    )
    @Operation(summary = "종합 피드백 작성 또는 수정", description = "멘토는 멘티의 플래너에 대한 종합 피드백을 작성합니다.")
    @ApiResponse(responseCode = "200", description = "종합 피드백 작성 성공")
    @PatchMapping("/daily-planner/{dailyPlannerId}/feedback")
    fun provideTotalFeedback(
        @AuthenticationPrincipal userId: Long,
        @Valid @RequestBody request: FeedbackDto.CreateTotalFeedbackRequest,
        @Parameter(
            name = "dailyPlannerId",
            description = "피드백을 작성할 플래너 ID",
            required = true,
            example = "1"
        )
        @PathVariable dailyPlannerId: Long
    ): ResponseEntity<Void> {

        feedbackService.provideTotalFeedbackForMenteesDailyPlanner(userId, dailyPlannerId, request)

        return ResponseEntity
            .status(HttpStatus.OK)
            .build()
    }

    @Operation(summary = "할 일의 피드백 요약, 상세 조회", description = "멘티는 멘토의 피드백 요약, 상세 내용을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "피드백 상세 조회 성공")
    @GetMapping("/tasks/{taskId}/feedback")
    fun getFeedbackOfTask(
        @AuthenticationPrincipal userId: Long,
        @Parameter(
            name = "taskId",
            description = "피드백을 조회할 할 일 ID",
            required = true,
            example = "1"
        )
        @PathVariable taskId: Long,
    ): ResponseEntity<FeedbackDto.GetFeedbackOfTaskResponse> = ResponseEntity
        .status(HttpStatus.OK)
        .body(
            feedbackService.findFeedbackOfTask(userId, taskId)
        )

    @Operation(summary = "특정 날짜 플래너의 종합 피드백 조회", description = "멘티는 멘토의 특정 날짜에 대한 종합 피드백을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "종합 피드백 조회 성공")
    @GetMapping("/daily-planner/{dailyPlannerId}/total-feedback")
    fun getTotalFeedbackOfDailyPlanner(
        @AuthenticationPrincipal userId: Long,
        @Parameter(
            name = "dailyPlannerId",
            description = "종합 피드백을 조회할 플래너 ID",
            required = true,
            example = "1"
        )
        @PathVariable dailyPlannerId: Long
    ): ResponseEntity<FeedbackDto.GetTotalFeedbackResponse> {

        val response = feedbackService.findTotalFeedbackOfDailyPlanner(userId, dailyPlannerId)

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(
                response
            )
    }

    /**
     * TODO: 멘토의 피드백 요약 목록 조회 / 멘티의 과목별 피드백 요약 목록 조회 API는 기획 및 디자인이 완성되면 구현 예ㅣ
     */
    @Operation(
        summary = "멘티별 피드백 목록 조회",
        description = "멘토가 멘티별로 피드백 목록을 조회합니다."
    )
    @ApiResponse(responseCode = "200", description = "멘티별 피드백 목록 조회 성공")
    @GetMapping("/mentees/{menteeId}/feedbacks")
    fun getFeedbacks(
        @AuthenticationPrincipal userId: Long,
        @Parameter(
            name = "menteeId",
            description = "피드백 필터 조건",
            required = true,
            example = "1"
        )
        @PathVariable menteeId: Long,
        @Parameter(
            name = "date",
            description = "조회할 날짜 (YYYY-MM-DD 형식)",
            example = "2026-02-03",
            required = true
        )
        @RequestParam date: LocalDate
    ): ResponseEntity<List<FeedbackDto.GetFeedbackOfTaskResponse>> = ResponseEntity
        .status(HttpStatus.OK)
        .body(feedbackService.findFeedbacksByMenteeId(menteeId, userId, date))
}
