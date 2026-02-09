package com.blaybus.backend.dto

import com.blaybus.backend.entity.Subject
import com.blaybus.backend.entity.TaskType
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.time.LocalDate

data class TaskWithFeedbackResponse(
    @Schema(description = "멘티 ID")
    val menteeId: Long,
    @Schema(description = "과제 및 피드백 목록")
    val tasks: PagedResponse<TaskDetail>,
    @Schema(description = "종합 피드백")
    val totalFeedback: String? = null,
)

data class TaskDetail(
    @Schema(description = "과제 ID")
    val taskId: Long,
    @Schema(description = "과제 과목")
    val subject: String,
    @Schema(description = "과제 제목")
    val title: String,
    @Schema(description = "투입 시간")
    val time: Int? = null,
    @Schema(description = "생성 날짜", example = "2026-02-06")
    val date: LocalDate,
    @Schema(description = "완료 여부")
    val status: Boolean,
    @Schema(description = "멘티 코멘트")
    val menteeComment: String? = null,
    @Schema(description = "피드백 여부")
    val feedbackStatus: String,
    @Schema(description = "과제 이미지 리스트")
    val images: List<TaskImageResponse>,
    val feedback: FeedbackDetail,
)

data class TaskImageResponse(
    @Schema(description = "과제 이미지 URL")
    val url: String,
    @Schema(description = "과제 이미지 이름")
    val name: String,
    @Schema(description = "과제 이미지 시퀀스")
    val sequence: Int,
)

data class FeedbackDetail(
    @Schema(description = "피드백 ID")
    val feedbackId: Long,
    @Schema(description = "피드백 코멘트")
    val content: String?,
)

data class MentorTaskAssignRequest(
    @field:Schema(description = "대상 멘티 ID", example = "101")
    @field:NotNull(message = "멘티 ID는 필수입니다")
    var menteeId: Long,
    @field:Schema(description = "과제 유형", allowableValues = ["COLUMN", "WEAKNESS_SOLUTION"])
    val taskType: TaskType,
    @field:Schema(description = "과제 제목", example = "수학 기출문제 풀이")
    @field:NotBlank(message = "제목은 필수입니다")
    val title: String,
    @field:Schema(description = "과제 상세 설명", example = "첨부된 PDF 3페이지부터 10페이지까지 풀기")
    val content: String? = null,
    @field:Schema(description = "과목", allowableValues = ["KOREAN", "MATH", "ENGLISH"])
    val subject: Subject,
    @field:Schema(description = "할당할 날짜", example = "2026-02-06")
    val date: LocalDate,
)
