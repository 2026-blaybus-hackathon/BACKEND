package com.blaybus.backend.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import java.time.LocalDateTime

class FeedbackDto {
    data class Summary(
        @field:Schema(
            description = "계속 유지하면 좋을 내용",
            example = "매일 꾸준히 학습하는 습관이 좋습니다",
            requiredMode = Schema.RequiredMode.REQUIRED,
        )
        @field:NotBlank(message = "계속 유지하면 좋을 내용 작성은 필수 입력값입니다")
        val keepContent: String,

        @field:Schema(
            description = "개선이 필요한 내용",
            example = "문제 풀이 시간을 조금 더 단축해보세요",
            requiredMode = Schema.RequiredMode.REQUIRED,
        )
        @field:NotBlank(message = "개선이 필요한 내용 작성은 필수 입력값입니다")
        val problemContent: String,

        @field:Schema(
            description = "시도해보길 권하는 내용",
            example = "타이머를 활용한 시간 제한 풀이를 시도해보세요",
            requiredMode = Schema.RequiredMode.REQUIRED,
        )
        @field:NotBlank(message = "시도해보길 권하는 내용 작성은 필수 입력값입니다")
        val tryContent: String,
    )
    data class CreateFeedbackRequest(
        @field:Schema(
            description = "KPT 형식의 피드백 요약",
            requiredMode = Schema.RequiredMode.REQUIRED,
        )
        @field:Valid
        val summary: Summary,

        @field:Schema(
            description = "피드백 상세 내용",
            example = "이번 주 학습 내용에 대한 전반적인 피드백입니다...",
            requiredMode = Schema.RequiredMode.REQUIRED,
        )
        @field:NotBlank(message = "피드백 상세 내용 작성은 필수 입력값입니다")
        val content: String,
    )

    // Response DTOs
    data class CreateFeedbackResponse(
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
