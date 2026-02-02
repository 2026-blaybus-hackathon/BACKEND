package com.blaybus.backend.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import java.time.LocalDateTime

class FeedbackDto {
    data class CreateFeedbackDetailRequest(
        @field:Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        @field:NotBlank(message = "피드백 작성은 필수 입력값입니다")
        val content: String,
    )

    data class CreateFeedbackSummaryRequest(
        @field:Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        @field:NotBlank(message = "피드백 요약 작성은 필수 입력값입니다")
        val summary: String,
    )

    // Response DTOs
    data class CreateFeedbackDetailResponse(
        @Schema(
            type = "number",
            description = "피드백 ID",
            example = "1")
        val id: Long,
    )

    data class CreateFeedbackSummaryResponse(
        @Schema(
            type = "number",
            description = "피드백 ID",
            example = "1")
        val id: Long,
    )

    data class GetFeedbackDetailResponse(
        @Schema(
            type = "number",
            description = "피드백 ID",
            example = "1")
        val id: Long,

        @Schema(
            type = "string",
            description = "피드백 상세 내용",
            example = "1")
        val content: String,
    )

    data class FeedbackSummaryResponse(
        @Schema(
            type = "number",
            description = "피드백 ID",
            example = "1")
        val id: Long,

        @Schema(
            type = "string",
            description = "과목명",
            example = "국어")
        val subject: String,

        @Schema(
            type = "string",
            description = "피드백 요약 내용",
            example = "독해력이 많이 향상되었습니다.")
        val summary: String,

        @Schema(
            description = "피드백 작성 시간",
            example = "2026-02-01T14:30:00")
        val createdAt: LocalDateTime,
    )
}
