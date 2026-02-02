package com.blaybus.backend.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

class FeedbackDto {
    data class CreateFeedbackSummaryRequest(
        @field:Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        @field:NotBlank(message = "피드백 요약 작성은 필수 입력값입니다")
        val summary: String,
    )
}
