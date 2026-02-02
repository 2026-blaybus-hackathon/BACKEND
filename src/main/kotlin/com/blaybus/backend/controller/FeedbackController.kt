package com.blaybus.backend.controller

import com.blaybus.backend.annotation.ApiErrorCodes
import com.blaybus.backend.dto.FeedbackDto
import com.blaybus.backend.exception.ErrorCode
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import jakarta.validation.Valid
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1")
class FeedbackController {
    private val logger = KotlinLogging.logger {}

    // TODO: 회의 후 피드백 요약 작성, 상세 작성 두 개의 API를 하나로 만들지 결정
    @ApiErrorCodes(
        ErrorCode.FORBIDDEN_FOR_CREATE_FEEDBACK,
    )
    @Operation(summary = "피드백 상세 작성", description = "멘토는 멘티의 할 일에 피드백 요약본을 작성합니다.")
    @ApiResponse(responseCode = "201", description = "피드백 요약 생성 성공")
    @PostMapping("/tasks/{taskId}/feedback")
    fun provideFeedback(
        @Valid @RequestBody request: FeedbackDto.CreateFeedbackDetailRequest,
        @PathVariable taskId: Long
    ): ResponseEntity<FeedbackDto.CreateFeedbackDetailResponse> {

        // 피드백을 작성할 수 있는 task인지 검증

        // 임시로 1L 응답
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(
                FeedbackDto.CreateFeedbackDetailResponse(1L)
            )
    }

    @ApiErrorCodes(
        ErrorCode.FORBIDDEN_FOR_CREATE_FEEDBACK,
    )
    @Operation(summary = "피드백 요약본 작성", description = "멘토는 멘티의 할 일에 피드백 요약본을 작성합니다.")
    @ApiResponse(responseCode = "201", description = "피드백 요약 생성 성공")
    @PostMapping("/tasks/{taskId}/feedback")
    fun provideFeedbackSummary(
        @Valid @RequestBody request: FeedbackDto.CreateFeedbackSummaryRequest,
        @PathVariable taskId: Long
    ): ResponseEntity<FeedbackDto.CreateFeedbackSummaryResponse> {

        // 피드백을 작성할 수 있는 task인지 검증

        // 임시로 1L 응답
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(
                FeedbackDto.CreateFeedbackSummaryResponse(1L)
            )
    }

}
