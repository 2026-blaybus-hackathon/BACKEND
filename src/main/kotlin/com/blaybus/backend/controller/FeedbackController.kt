package com.blaybus.backend.controller

import com.blaybus.backend.annotation.ApiErrorCodes
import com.blaybus.backend.dto.FeedbackDto
import com.blaybus.backend.exception.ErrorCode
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController("/api/v1/tasks/{taskId}/feedback")
class FeedbackController {
    @ApiErrorCodes(
        ErrorCode.FORBIDDEN_FOR_CREATE_FEEDBACK,
    )
    @Operation(summary = "피드백 요약본 작성", description = "멘토는 멘티의 할 일에 피드백 요약본을 작성합니다.")
    @ApiResponse(responseCode = "201", description = "피드백 요약 생성 성공")
    @PostMapping
    fun provideFeedbackSummary(
        @Valid @RequestBody request: FeedbackDto.CreateFeedbackSummaryRequest,
        @PathVariable taskId: Long
    ): ResponseEntity<Void> {

        // 피드백을 작성할 수 있는 task인지 검증


        return ResponseEntity
            .status(HttpStatus.CREATED)
            .build()
    }
}
